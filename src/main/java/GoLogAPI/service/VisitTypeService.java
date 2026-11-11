package GoLogAPI.service;

import GoLogAPI.dto.visitType.VisitTypeCreateRequest;
import GoLogAPI.dto.visitType.VisitTypeResponse;
import GoLogAPI.dto.visitType.VisitTypeUpdateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.VisitTypeMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.VisitType;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.VisitTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VisitTypeService {

    private final VisitTypeRepository visitTypeRepository;
    private final CompanyRepository companyRepository;
    private final VisitTypeMapper visitTypeMapper;

    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public VisitTypeService(VisitTypeRepository visitTypeRepository,
                            CompanyRepository companyRepository,
                            VisitTypeMapper visitTypeMapper,
            GoLogAPI.infra.tenant.TenantGuard tenantGuard) {
        this.tenantGuard = tenantGuard;
        this.visitTypeRepository = visitTypeRepository;
        this.companyRepository = companyRepository;
        this.visitTypeMapper = visitTypeMapper;
    }

    @Transactional
    public VisitTypeResponse save(VisitTypeCreateRequest request) {
        Company owner = tenantGuard.ownerForCatalog(request.companyId());
        UUID ownerId = owner != null ? owner.getId() : null;
        boolean duplicated = visitTypeRepository.findAvailableByCompanyId(ownerId).stream()
                .anyMatch(v -> v.getCode() != null && v.getCode().equalsIgnoreCase(request.code())
                        && (v.getCompany() == null || ownerId == null || v.getCompany().getId().equals(ownerId)));
        if (duplicated) {
            throw new ConflictException("Já existe um tipo de visita com o código '" + request.code() + "' disponível para esta empresa.");
        }

        VisitType visitType = visitTypeMapper.toEntity(request);
        visitType.setCompany(owner);

        visitTypeRepository.save(visitType);
        return visitTypeMapper.toResponse(visitType);
    }

    public VisitTypeResponse get(UUID id) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogRead(visitType.getCompany(), id);
        return visitTypeMapper.toResponse(visitType);
    }

    public List<VisitTypeResponse> getAll() {
        return visitTypeMapper.toResponses(visitTypeRepository.findAvailableByCompanyId(tenantGuard.scope()));
    }

    public List<VisitTypeResponse> getAvailableForCompany(UUID companyId) {
        // Fora da Visão Global, só é possível consultar o catálogo do próprio tenant
        if (!tenantGuard.isGlobalView()) companyId = tenantGuard.scope();
        if (companyId == null) {
            return visitTypeMapper.toResponses(visitTypeRepository.findAll());
        }
        return visitTypeMapper.toResponses(visitTypeRepository.findAvailableByCompanyId(companyId));
    }

    @Transactional
    public void delete(UUID id) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(visitType.getCompany(), id);
        visitTypeRepository.delete(visitType);
    }

    @Transactional
    public VisitTypeResponse update(UUID id, VisitTypeUpdateRequest request) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(visitType.getCompany(), id);

        visitTypeMapper.updateFromDto(request, visitType);
        visitTypeRepository.save(visitType);
        return visitTypeMapper.toResponse(visitType);
    }
}
