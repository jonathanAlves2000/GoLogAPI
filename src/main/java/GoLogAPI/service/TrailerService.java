package GoLogAPI.service;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.dto.trailer.TrailerUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.TrailerMapper;
import GoLogAPI.model.Trailer;
import GoLogAPI.repository.TrailerRepository;
import GoLogAPI.validation.TrailerValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Carretas / reboques. Mesmas regras de propriedade de {@link TractorService}. */
@Service
@Transactional(readOnly = true)
public class TrailerService {

    private final TrailerRepository trailerRepository;
    private final TrailerMapper trailerMapper;
    private final TrailerValidate trailerValidate;
    private final TenantGuard tenantGuard;

    public TrailerService(TrailerRepository trailerRepository, TrailerMapper trailerMapper,
                          TrailerValidate trailerValidate, TenantGuard tenantGuard){
        this.trailerRepository = trailerRepository;
        this.trailerMapper = trailerMapper;
        this.trailerValidate = trailerValidate;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public TrailerResponse save(TrailerCreateRequest request){
        trailerValidate.validate(request);
        Trailer trailer = trailerMapper.toEntity(request);
        trailer.setCompany(tenantGuard.ownerForWrite(request.companyId()));
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public TrailerResponse get(UUID id){
        return trailerMapper.toResponse(findVisible(id));
    }

    public List<TrailerResponse> getAll() {
        List<Trailer> trailers = tenantGuard.isGlobalView()
                ? trailerRepository.findAll()
                : trailerRepository.findByCompanyId(tenantGuard.scope());
        return trailerMapper.toResponses(trailers);
    }

    @Transactional
    public void delete(UUID id){
        trailerRepository.delete(findVisible(id));
    }

    @Transactional
    public TrailerResponse update(UUID id, TrailerCreateRequest request) {
        Trailer trailer = findVisible(id);
        trailerValidate.validate(request, id);
        trailer.setPlate(request.plate());
        trailer.setRenavam(request.renavam());
        trailer.setModel(request.model());
        trailer.setNumberAxles(request.numberAxles());
        trailer.setMaximumCapacity(request.maximumCapacity());
        trailer.setMaximumVolume(request.maximumVolume());
        trailer.setStatus(request.status());
        trailer.setCompany(tenantGuard.ownerForWrite(request.companyId()));
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    @Transactional
    public TrailerResponse updatePartial(UUID id, TrailerUpdateRequest request){
        Trailer trailer = findVisible(id);
        trailerValidate.validate(request, id);

        if(request.plate() != null && !request.plate().isBlank())
            trailer.setPlate(request.plate());
        if(request.renavam() != null && !request.renavam().isBlank())
            trailer.setRenavam(request.renavam());
        if(request.model() != null && !request.model().isBlank())
            trailer.setModel(request.model());
        if(request.numberAxles() != null)
            trailer.setNumberAxles(request.numberAxles());
        if(request.maximumCapacity() != null)
            trailer.setMaximumCapacity(request.maximumCapacity());
        if(request.maximumVolume() != null)
            trailer.setMaximumVolume(request.maximumVolume());
        if(request.companyId() != null)
            trailer.setCompany(tenantGuard.ownerForWrite(request.companyId()));

        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public Trailer findVisible(UUID id) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carreta não encontrada: ", id));
        tenantGuard.checkAccess(trailer.getCompany(), id);
        return trailer;
    }
}
