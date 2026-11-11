package GoLogAPI.service;

import GoLogAPI.dto.company.*;
import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.CompanyMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.model.enums.CompanyType;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.validation.CompanyValidate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cadastro de empresas parceiras (clientes e fornecedores) da carteira de cada tenant.
 *
 * <p>Empresas-tenant (matriz/filial/master) são criadas exclusivamente por {@code /tenants}. Por aqui
 * um tenant pode apenas consultar e editar os próprios dados cadastrais.</p>
 */
@Service
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final CompanyMapper companyMapper;
    private final CompanyValidate companyValidate;
    private final GoLogAPI.service.webhook.WebhookDispatcherService webhookDispatcherService;
    private final TenantGuard tenantGuard;

    public CompanyService(CompanyRepository companyRepository,
                          AddressRepository addressRepository,
                          CompanyMapper companyMapper,
                          CompanyValidate companyValidate,
                          GoLogAPI.service.webhook.WebhookDispatcherService webhookDispatcherService,
                          TenantGuard tenantGuard){
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.companyMapper = companyMapper;
        this.companyValidate = companyValidate;
        this.webhookDispatcherService = webhookDispatcherService;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public CompanyCreateResponse save(CompanyCreateRequest request){
        Company owner = tenantGuard.ownerForWrite(null);
        companyValidate.validate(request, owner.getId());

        Company company = companyMapper.toEntity(request);
        company.setAddress(findAddress(request.addressId()));
        company.setCompanyType(Boolean.TRUE.equals(request.isCliente()) ? CompanyType.CLIENT : CompanyType.SUPPLIER);
        company.setIsMaster(false);
        company.setOwnerTenant(owner);
        companyRepository.save(company);
        return companyMapper.toCreateResponse(company);
    }

    public CompanyResponse get(UUID id){
        return companyMapper.toResponse(findVisible(id));
    }

    public List<CompanyResponseList> getAll(){
        List<Company> companies = tenantGuard.isGlobalView()
                ? companyRepository.findAll()
                : companyRepository.findVisibleForTenant(tenantGuard.scope());
        return companyMapper.toResponses(companies);
    }

    @Transactional
    public void delete(UUID id){
        Company company = findVisible(id);
        if (Boolean.TRUE.equals(company.getIsMaster()) || TenantGuard.isTenantCompany(company)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Empresas-tenant (matriz/filial) não podem ser excluídas pelo cadastro de empresas. Use a Gestão de Tenants.");
        }
        companyRepository.delete(company);
    }

    @Transactional
    public CompanyCreateResponse update(UUID id, CompanyCreateRequest request){
        Company company = findVisible(id);
        companyValidate.validate(request, ownerId(company), id);

        company.setLegalName(request.legalName());
        company.setPhoneNumber(request.phoneNumber());
        company.setEmail(request.email());
        company.setCnpjCpf(request.cnpjCpf());
        company.setAddress(findAddress(request.addressId()));
        applyClientFlag(company, request.isCliente());
        companyRepository.save(company);
        return companyMapper.toCreateResponse(company);
    }

    @Transactional
    public CompanyCreateResponse updatePartial(UUID id, CompanyUpdateRequest request){
        Company company = findVisible(id);
        companyValidate.validate(request, ownerId(company), id);

        if(request.cnpjCpf() != null && !request.cnpjCpf().isBlank())
            company.setCnpjCpf(request.cnpjCpf());
        if(request.isCliente() != null)
            applyClientFlag(company, request.isCliente());
        if(request.email() != null && !request.email().isBlank())
            company.setEmail(request.email());
        if(request.legalName() != null && !request.legalName().isBlank())
            company.setLegalName(request.legalName());
        if(request.phoneNumber() != null && !request.phoneNumber().isBlank())
            company.setPhoneNumber(request.phoneNumber());
        if(request.addressId() != null)
            company.setAddress(findAddress(request.addressId()));
        companyRepository.save(company);
        return companyMapper.toCreateResponse(company);
    }

    @Transactional
    public CompanyResponse updateWebhook(UUID id, GoLogAPI.dto.webhook.WebhookConfigRequest request) {
        Company company = findVisible(id);
        company.setWebhookUrl(request.webhookUrl());
        company.setWebhookSecret(request.webhookSecret());
        company.setWebhookActive(request.webhookActive() != null ? request.webhookActive() : true);
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    public GoLogAPI.dto.webhook.WebhookTestResponse testWebhook(UUID id) {
        Company company = findVisible(id);
        if (company.getWebhookUrl() == null || company.getWebhookUrl().isBlank()) {
            return new GoLogAPI.dto.webhook.WebhookTestResponse(
                    false, 0, null, 0L, "Empresa não possui URL de webhook configurada.");
        }
        return webhookDispatcherService.testWebhook(company.getWebhookUrl(), company.getWebhookSecret());
    }

    public GoLogAPI.dto.webhook.WebhookTestResponse testWebhookUrl(String url, String secret) {
        return webhookDispatcherService.testWebhook(url, secret);
    }

    /** Busca uma empresa visível no escopo: o próprio tenant, suas filiais ou sua carteira. */
    public Company findVisible(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        if (!canSee(company)) {
            throw new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id);
        }
        return company;
    }

    private boolean canSee(Company c) {
        if (tenantGuard.isGlobalView()) return true;
        UUID scope = tenantGuard.scope();
        return Objects.equals(c.getId(), scope)
                || (c.getParentCompany() != null && Objects.equals(c.getParentCompany().getId(), scope))
                || (c.getOwnerTenant() != null && Objects.equals(c.getOwnerTenant().getId(), scope));
    }

    private void applyClientFlag(Company company, Boolean isCliente) {
        if (isCliente == null) return;
        company.setIsCliente(isCliente);
        // Só reclassifica parceiros; o tipo de uma empresa-tenant nunca muda por aqui.
        if (!TenantGuard.isTenantCompany(company)) {
            company.setCompanyType(isCliente ? CompanyType.CLIENT : CompanyType.SUPPLIER);
        } else {
            company.setIsCliente(false);
        }
    }

    private static UUID ownerId(Company company) {
        return company.getOwnerTenant() != null ? company.getOwnerTenant().getId() : null;
    }

    private Address findAddress(UUID addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: ", addressId));
    }
}
