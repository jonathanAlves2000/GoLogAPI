package GoLogAPI.service;

import GoLogAPI.dto.visitTypeRule.VisitTypeRuleCreateRequest;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleResponse;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleUpdateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.VisitTypeRuleMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.VisitType;
import GoLogAPI.model.VisitTypeRule;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.VisitTypeRepository;
import GoLogAPI.repository.VisitTypeRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VisitTypeRuleService {

    private final VisitTypeRuleRepository visitTypeRuleRepository;
    private final VisitTypeRepository visitTypeRepository;
    private final CompanyRepository companyRepository;
    private final VisitTypeRuleMapper visitTypeRuleMapper;

    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public VisitTypeRuleService(VisitTypeRuleRepository visitTypeRuleRepository,
                                VisitTypeRepository visitTypeRepository,
                                CompanyRepository companyRepository,
                                VisitTypeRuleMapper visitTypeRuleMapper,
            GoLogAPI.infra.tenant.TenantGuard tenantGuard) {
        this.tenantGuard = tenantGuard;
        this.visitTypeRuleRepository = visitTypeRuleRepository;
        this.visitTypeRepository = visitTypeRepository;
        this.companyRepository = companyRepository;
        this.visitTypeRuleMapper = visitTypeRuleMapper;
    }

    @Transactional
    public VisitTypeRuleResponse save(VisitTypeRuleCreateRequest request) {
        if (request.visitType1Id().equals(request.visitType2Id())) {
            throw new ConflictException("Uma regra de incompatibilidade/exigência não pode ser associada ao mesmo tipo de visita.");
        }

        VisitType visitType1 = findVisitType(request.visitType1Id());
        VisitType visitType2 = findVisitType(request.visitType2Id());

        VisitTypeRule rule = visitTypeRuleMapper.toEntity(request);
        rule.setVisitType1(visitType1);
        rule.setVisitType2(visitType2);

        rule.setCompany(tenantGuard.ownerForCatalog(request.companyId()));

        visitTypeRuleRepository.save(rule);
        return visitTypeRuleMapper.toResponse(rule);
    }

    public VisitTypeRuleResponse get(UUID id) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogRead(rule.getCompany(), id);
        return visitTypeRuleMapper.toResponse(rule);
    }

    public List<VisitTypeRuleResponse> getByCompany(UUID companyId) {
        // Fora da Visão Global, só é possível consultar o catálogo do próprio tenant
        if (!tenantGuard.isGlobalView()) companyId = tenantGuard.scope();
        if (companyId == null) {
            return visitTypeRuleMapper.toResponses(visitTypeRuleRepository.findAll());
        }
        return visitTypeRuleMapper.toResponses(visitTypeRuleRepository.findAvailableByCompanyId(companyId));
    }

    @Transactional
    public void delete(UUID id) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(rule.getCompany(), id);
        visitTypeRuleRepository.delete(rule);
    }

    @Transactional
    public VisitTypeRuleResponse update(UUID id, VisitTypeRuleUpdateRequest request) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(rule.getCompany(), id);

        if (request.visitType1Id() != null) {
            VisitType visitType1 = findVisitType(request.visitType1Id());
            rule.setVisitType1(visitType1);
        }

        if (request.visitType2Id() != null) {
            VisitType visitType2 = findVisitType(request.visitType2Id());
            rule.setVisitType2(visitType2);
        }

        visitTypeRuleMapper.updateFromDto(request, rule);
        visitTypeRuleRepository.save(rule);
        return visitTypeRuleMapper.toResponse(rule);
    }

    private VisitType findVisitType(UUID id) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de visita não encontrado: ", id));
        tenantGuard.checkCatalogRead(visitType.getCompany(), id);
        return visitType;
    }
}
