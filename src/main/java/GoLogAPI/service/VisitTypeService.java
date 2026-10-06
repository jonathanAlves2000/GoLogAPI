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

    public VisitTypeService(VisitTypeRepository visitTypeRepository,
                            CompanyRepository companyRepository,
                            VisitTypeMapper visitTypeMapper) {
        this.visitTypeRepository = visitTypeRepository;
        this.companyRepository = companyRepository;
        this.visitTypeMapper = visitTypeMapper;
    }

    @Transactional
    public VisitTypeResponse save(VisitTypeCreateRequest request) {
        if (request.companyId() != null && visitTypeRepository.existsByCodeAndCompanyId(request.code(), request.companyId())) {
            throw new ConflictException("Já existe um tipo de visita com este código para esta empresa.");
        }

        VisitType visitType = visitTypeMapper.toEntity(request);

        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.companyId()));
            visitType.setCompany(company);
        }

        visitTypeRepository.save(visitType);
        return visitTypeMapper.toResponse(visitType);
    }

    public VisitTypeResponse get(UUID id) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return visitTypeMapper.toResponse(visitType);
    }

    public List<VisitTypeResponse> getAll() {
        return visitTypeMapper.toResponses(visitTypeRepository.findAll());
    }

    public List<VisitTypeResponse> getAvailableForCompany(UUID companyId) {
        if (companyId == null) {
            return visitTypeMapper.toResponses(visitTypeRepository.findAll());
        }
        return visitTypeMapper.toResponses(visitTypeRepository.findAvailableByCompanyId(companyId));
    }

    @Transactional
    public void delete(UUID id) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        visitTypeRepository.delete(visitType);
    }

    @Transactional
    public VisitTypeResponse update(UUID id, VisitTypeUpdateRequest request) {
        VisitType visitType = visitTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        visitTypeMapper.updateFromDto(request, visitType);
        visitTypeRepository.save(visitType);
        return visitTypeMapper.toResponse(visitType);
    }
}
