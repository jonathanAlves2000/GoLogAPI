package GoLogAPI.service;

import GoLogAPI.dto.optimizationProfile.OptimizationProfileCreateRequest;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileResponse;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.OptimizationProfileMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.OptimizationProfile;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.OptimizationProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OptimizationProfileService {

    private final OptimizationProfileRepository optimizationProfileRepository;
    private final CompanyRepository companyRepository;
    private final OptimizationProfileMapper optimizationProfileMapper;

    public OptimizationProfileService(OptimizationProfileRepository optimizationProfileRepository,
                                      CompanyRepository companyRepository,
                                      OptimizationProfileMapper optimizationProfileMapper) {
        this.optimizationProfileRepository = optimizationProfileRepository;
        this.companyRepository = companyRepository;
        this.optimizationProfileMapper = optimizationProfileMapper;
    }

    @Transactional
    public OptimizationProfileResponse save(OptimizationProfileCreateRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.companyId()));

        OptimizationProfile profile = optimizationProfileMapper.toEntity(request);
        profile.setCompany(company);

        // Se for marcado como default, remove o default dos outros perfis da empresa
        if (Boolean.TRUE.equals(request.isDefault())) {
            unsetPreviousDefault(company.getId());
            profile.setIsDefault(true);
        } else {
            // Se for o primeiro perfil da empresa, força como default
            List<OptimizationProfile> existingProfiles = optimizationProfileRepository.findByCompanyId(company.getId());
            if (existingProfiles.isEmpty()) {
                profile.setIsDefault(true);
            }
        }

        optimizationProfileRepository.save(profile);
        return optimizationProfileMapper.toResponse(profile);
    }

    public OptimizationProfileResponse get(UUID id) {
        OptimizationProfile profile = optimizationProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return optimizationProfileMapper.toResponse(profile);
    }

    public List<OptimizationProfileResponse> getByCompany(UUID companyId) {
        return optimizationProfileMapper.toResponses(optimizationProfileRepository.findByCompanyId(companyId));
    }

    public OptimizationProfileResponse getDefaultByCompany(UUID companyId) {
        OptimizationProfile profile = optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(companyId)
                .or(() -> optimizationProfileRepository.findFirstByCompanyId(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum perfil de otimização encontrado para a empresa: ", companyId));
        return optimizationProfileMapper.toResponse(profile);
    }

    @Transactional
    public void delete(UUID id) {
        OptimizationProfile profile = optimizationProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        optimizationProfileRepository.delete(profile);
    }

    @Transactional
    public OptimizationProfileResponse update(UUID id, OptimizationProfileUpdateRequest request) {
        OptimizationProfile profile = optimizationProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if (Boolean.TRUE.equals(request.isDefault()) && !Boolean.TRUE.equals(profile.getIsDefault())) {
            unsetPreviousDefault(profile.getCompany().getId());
        }

        optimizationProfileMapper.updateFromDto(request, profile);
        optimizationProfileRepository.save(profile);
        return optimizationProfileMapper.toResponse(profile);
    }

    private void unsetPreviousDefault(UUID companyId) {
        optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(companyId)
                .ifPresent(p -> {
                    p.setIsDefault(false);
                    optimizationProfileRepository.save(p);
                });
    }
}
