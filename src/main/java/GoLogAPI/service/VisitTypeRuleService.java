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

    public VisitTypeRuleService(VisitTypeRuleRepository visitTypeRuleRepository,
                                VisitTypeRepository visitTypeRepository,
                                CompanyRepository companyRepository,
                                VisitTypeRuleMapper visitTypeRuleMapper) {
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

        VisitType visitType1 = visitTypeRepository.findById(request.visitType1Id())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.visitType1Id()));

        VisitType visitType2 = visitTypeRepository.findById(request.visitType2Id())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.visitType2Id()));

        VisitTypeRule rule = visitTypeRuleMapper.toEntity(request);
        rule.setVisitType1(visitType1);
        rule.setVisitType2(visitType2);

        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.companyId()));
            rule.setCompany(company);
        }

        visitTypeRuleRepository.save(rule);
        return visitTypeRuleMapper.toResponse(rule);
    }

    public VisitTypeRuleResponse get(UUID id) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return visitTypeRuleMapper.toResponse(rule);
    }

    public List<VisitTypeRuleResponse> getByCompany(UUID companyId) {
        if (companyId == null) {
            return visitTypeRuleMapper.toResponses(visitTypeRuleRepository.findAll());
        }
        return visitTypeRuleMapper.toResponses(visitTypeRuleRepository.findAvailableByCompanyId(companyId));
    }

    @Transactional
    public void delete(UUID id) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        visitTypeRuleRepository.delete(rule);
    }

    @Transactional
    public VisitTypeRuleResponse update(UUID id, VisitTypeRuleUpdateRequest request) {
        VisitTypeRule rule = visitTypeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if (request.visitType1Id() != null) {
            VisitType visitType1 = visitTypeRepository.findById(request.visitType1Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.visitType1Id()));
            rule.setVisitType1(visitType1);
        }

        if (request.visitType2Id() != null) {
            VisitType visitType2 = visitTypeRepository.findById(request.visitType2Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.visitType2Id()));
            rule.setVisitType2(visitType2);
        }

        visitTypeRuleMapper.updateFromDto(request, rule);
        visitTypeRuleRepository.save(rule);
        return visitTypeRuleMapper.toResponse(rule);
    }
}
