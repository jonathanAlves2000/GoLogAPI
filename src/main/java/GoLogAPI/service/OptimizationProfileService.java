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
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public OptimizationProfileService(OptimizationProfileRepository optimizationProfileRepository,
                                      CompanyRepository companyRepository,
                                      OptimizationProfileMapper optimizationProfileMapper,
                                      GoLogAPI.infra.tenant.TenantGuard tenantGuard) {
        this.tenantGuard = tenantGuard;
        this.optimizationProfileRepository = optimizationProfileRepository;
        this.companyRepository = companyRepository;
        this.optimizationProfileMapper = optimizationProfileMapper;
    }

    @Transactional
    public OptimizationProfileResponse save(OptimizationProfileCreateRequest request) {
        Company company = tenantGuard.ownerForWrite(request.companyId());

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
        OptimizationProfile profile = findVisible(id);
        return optimizationProfileMapper.toResponse(profile);
    }

    public List<OptimizationProfileResponse> getAll() {
        return optimizationProfileMapper.toResponses(tenantGuard.isGlobalView()
                ? optimizationProfileRepository.findAll()
                : optimizationProfileRepository.findByCompanyId(tenantGuard.scope()));
    }

    public List<OptimizationProfileResponse> getByCompany(UUID companyId) {
        if (!tenantGuard.isGlobalView() && !companyId.equals(tenantGuard.scope())) {
            return List.of();
        }
        return optimizationProfileMapper.toResponses(optimizationProfileRepository.findByCompanyId(companyId));
    }

    public OptimizationProfileResponse getDefaultByCompany(UUID companyId) {
        if (!tenantGuard.isGlobalView() && !companyId.equals(tenantGuard.scope())) {
            throw new ResourceNotFoundException("Nenhum perfil de otimização encontrado para a empresa: ", companyId);
        }
        OptimizationProfile profile = optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(companyId)
                .or(() -> optimizationProfileRepository.findFirstByCompanyId(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum perfil de otimização encontrado para a empresa: ", companyId));
        return optimizationProfileMapper.toResponse(profile);
    }

    @Transactional
    public void delete(UUID id) {
        OptimizationProfile profile = findVisible(id);
        optimizationProfileRepository.delete(profile);
    }

    @Transactional
    public OptimizationProfileResponse update(UUID id, OptimizationProfileUpdateRequest request) {
        OptimizationProfile profile = findVisible(id);

        if (Boolean.TRUE.equals(request.isDefault()) && !Boolean.TRUE.equals(profile.getIsDefault())) {
            unsetPreviousDefault(profile.getCompany().getId());
        }

        optimizationProfileMapper.updateFromDto(request, profile);
        optimizationProfileRepository.save(profile);
        return optimizationProfileMapper.toResponse(profile);
    }

    public OptimizationProfile findVisible(UUID id) {
        OptimizationProfile profile = optimizationProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de otimização não encontrado: ", id));
        tenantGuard.checkAccess(profile.getCompany(), id);
        return profile;
    }

    private void unsetPreviousDefault(UUID companyId) {
        optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(companyId)
                .ifPresent(p -> {
                    p.setIsDefault(false);
                    optimizationProfileRepository.save(p);
                });
    }
}
