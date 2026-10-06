package GoLogAPI.service;

import GoLogAPI.dto.demandType.DemandTypeCreateRequest;
import GoLogAPI.dto.demandType.DemandTypeResponse;
import GoLogAPI.dto.demandType.DemandTypeUpdateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.DemandTypeMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.DemandType;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.DemandTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DemandTypeService {

    private final DemandTypeRepository demandTypeRepository;
    private final CompanyRepository companyRepository;
    private final DemandTypeMapper demandTypeMapper;

    public DemandTypeService(DemandTypeRepository demandTypeRepository,
                             CompanyRepository companyRepository,
                             DemandTypeMapper demandTypeMapper) {
        this.demandTypeRepository = demandTypeRepository;
        this.companyRepository = companyRepository;
        this.demandTypeMapper = demandTypeMapper;
    }

    @Transactional
    public DemandTypeResponse save(DemandTypeCreateRequest request) {
        if (request.companyId() != null && demandTypeRepository.existsByCodeAndCompanyId(request.code(), request.companyId())) {
            throw new ConflictException("Já existe um tipo de demanda com este código para esta empresa.");
        }

        DemandType demandType = demandTypeMapper.toEntity(request);
        demandType.setIsSystemDefault(false);

        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.companyId()));
            demandType.setCompany(company);
        }

        demandTypeRepository.save(demandType);
        return demandTypeMapper.toResponse(demandType);
    }

    public DemandTypeResponse get(UUID id) {
        DemandType demandType = demandTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return demandTypeMapper.toResponse(demandType);
    }

    public List<DemandTypeResponse> getAll() {
        return demandTypeMapper.toResponses(demandTypeRepository.findAll());
    }

    public List<DemandTypeResponse> getAvailableForCompany(UUID companyId) {
        if (companyId == null) {
            return demandTypeMapper.toResponses(demandTypeRepository.findByIsSystemDefaultTrue());
        }
        return demandTypeMapper.toResponses(demandTypeRepository.findAvailableByCompanyId(companyId));
    }

    @Transactional
    public void delete(UUID id) {
        DemandType demandType = demandTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if (Boolean.TRUE.equals(demandType.getIsSystemDefault())) {
            throw new ConflictException("Não é permitido excluir tipos de demanda padrão do sistema.");
        }

        demandTypeRepository.delete(demandType);
    }

    @Transactional
    public DemandTypeResponse update(UUID id, DemandTypeUpdateRequest request) {
        DemandType demandType = demandTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        demandTypeMapper.updateFromDto(request, demandType);
        demandTypeRepository.save(demandType);
        return demandTypeMapper.toResponse(demandType);
    }
}
