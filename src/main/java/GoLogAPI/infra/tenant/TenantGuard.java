package GoLogAPI.infra.tenant;

import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Company;
import GoLogAPI.model.enums.CompanyType;
import GoLogAPI.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Ponto único de decisão de isolamento multi-tenant.
 *
 * <p>Regras adotadas em toda a API:</p>
 * <ul>
 *   <li><b>Escopo</b> ({@link #scope()}): tenant ativo da requisição, resolvido pelo {@code SecurityFilter}
 *       a partir do JWT e do header {@code X-Tenant-Id}. É {@code null} apenas para o Master GoLog em
 *       "Visão Global".</li>
 *   <li><b>Dados operacionais</b> (frota, conjuntos, escalas, remessas, transportes, ocorrências, endereços,
 *       motoristas, usuários, perfis de otimização): sempre pertencem a um tenant. Leitura e escrita só
 *       dentro do escopo; na Visão Global o Master enxerga tudo.</li>
 *   <li><b>Catálogos</b> (tipos de transporte, de carga, de visita, de demanda, modelos de jornada e regras
 *       de visita): registros com {@code company = null} são padrões do sistema, visíveis para todos e
 *       editáveis apenas pelo Master.</li>
 * </ul>
 *
 * <p>Acesso a um registro fora do escopo responde 404 (e não 403) para não revelar a existência de dados
 * de outros tenants.</p>
 */
@Component
public class TenantGuard {

    private static final String NOT_FOUND = "Registro não encontrado: ";

    private final CompanyRepository companyRepository;

    public TenantGuard(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /** Tenant ativo da requisição ({@code null} = Visão Global do Master). */
    public UUID scope() {
        return TenantContext.getCurrentTenantId();
    }

    public boolean isMaster() {
        return TenantContext.isMaster();
    }

    /** Master sem tenant selecionado: enxerga e opera sobre todos os tenants. */
    public boolean isGlobalView() {
        return TenantContext.isMaster() && TenantContext.getCurrentTenantId() == null;
    }

    public static boolean isTenantCompany(Company company) {
        if (company == null) return false;
        CompanyType type = company.getCompanyType();
        if (type != null) {
            return type == CompanyType.TENANT_MASTER
                    || type == CompanyType.TENANT_HEADQUARTER
                    || type == CompanyType.TENANT_BRANCH;
        }
        return Boolean.TRUE.equals(company.getIsMaster()) || !Boolean.TRUE.equals(company.getIsCliente());
    }

    /**
     * Resolve o tenant dono de um novo registro operacional.
     *
     * @param requestedCompanyId empresa informada explicitamente no formulário (opcional). Quando informada,
     *                           precisa ser um tenant (transportadora) e estar no escopo do usuário.
     * @return tenant dono — nunca {@code null}.
     */
    public Company ownerForWrite(UUID requestedCompanyId) {
        if (requestedCompanyId != null) {
            Company requested = companyRepository.findById(requestedCompanyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: ", requestedCompanyId));
            if (!isTenantCompany(requested)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "A empresa '" + requested.getLegalName() + "' é um cliente/fornecedor e não pode ser dona deste cadastro. "
                                + "Selecione a transportadora (tenant) responsável.");
            }
            if (!isGlobalView() && !Objects.equals(requested.getId(), scope())
                    && !(isMaster() || isBranchOf(requested, scope()))) {
                throw new BusinessException(HttpStatus.FORBIDDEN,
                        "Você não tem permissão para cadastrar registros na empresa '" + requested.getLegalName() + "'.");
            }
            return requested;
        }

        UUID ownerId = scope() != null ? scope() : TenantContext.getUserCompanyId();
        if (ownerId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Seu usuário não está vinculado a nenhuma empresa. Vincule-o a um tenant antes de cadastrar registros.");
        }
        return companyRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant ativo não encontrado: ", ownerId));
    }

    /**
     * Resolve o dono de um registro de catálogo. O Master em Visão Global cria padrões do sistema
     * ({@code null}); nos demais casos o catálogo pertence ao tenant ativo.
     */
    public Company ownerForCatalog(UUID requestedCompanyId) {
        if (requestedCompanyId != null) {
            return ownerForWrite(requestedCompanyId);
        }
        if (isGlobalView()) {
            return null;
        }
        return ownerForWrite(null);
    }

    /** {@code true} se o registro operacional pertencente a {@code owner} está visível no escopo atual. */
    public boolean canSee(Company owner) {
        if (isGlobalView()) return true;
        return owner != null && Objects.equals(owner.getId(), scope());
    }

    /** Garante que um registro operacional está no escopo; caso contrário responde 404. */
    public void checkAccess(Company owner, UUID id) {
        if (!canSee(owner)) {
            throw new ResourceNotFoundException(NOT_FOUND, id);
        }
    }

    /** Leitura de catálogo: padrões do sistema ({@code owner == null}) são visíveis para todos. */
    public void checkCatalogRead(Company owner, UUID id) {
        if (owner == null) return;
        checkAccess(owner, id);
    }

    /** Escrita de catálogo: padrões do sistema só podem ser alterados pelo Master. */
    public void checkCatalogWrite(Company owner, UUID id) {
        if (owner == null) {
            if (!isMaster()) {
                throw new BusinessException(HttpStatus.FORBIDDEN,
                        "Registros padrão do sistema só podem ser alterados pela administração GoLog Master.");
            }
            return;
        }
        checkAccess(owner, id);
    }

    private boolean isBranchOf(Company company, UUID parentId) {
        return parentId != null && company.getParentCompany() != null
                && Objects.equals(company.getParentCompany().getId(), parentId);
    }
}
