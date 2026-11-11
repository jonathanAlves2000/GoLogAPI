package GoLogAPI.service;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.dto.tractor.TractorUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.TractorMapper;
import GoLogAPI.model.Tractor;
import GoLogAPI.repository.TractorRepository;
import GoLogAPI.validation.TractorValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Cavalos mecânicos / caminhões (unidades de tração). O dono é sempre um tenant (transportadora): se o
 * formulário enviar um {@code companyId}, ele precisa ser um tenant do escopo; caso contrário usa o
 * tenant ativo.
 */
@Service
@Transactional(readOnly = true)
public class TractorService {

    private final TractorRepository tractorRepository;
    private final TractorMapper tractorMapper;
    private final TractorValidate tractorValidate;
    private final TenantGuard tenantGuard;

    public TractorService(TractorRepository tractorRepository, TractorMapper tractorMapper,
                          TractorValidate tractorValidate, TenantGuard tenantGuard){
        this.tractorRepository = tractorRepository;
        this.tractorMapper = tractorMapper;
        this.tractorValidate = tractorValidate;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public TractorResponse save(TractorCreateRequest request){
        tractorValidate.validate(request);
        Tractor tractor = tractorMapper.toEntity(request);
        tractor.setCompany(tenantGuard.ownerForWrite(request.companyId()));
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public TractorResponse get(UUID id){
        return tractorMapper.toResponse(findVisible(id));
    }

    public List<TractorResponse> getAll() {
        List<Tractor> tractors = tenantGuard.isGlobalView()
                ? tractorRepository.findAll()
                : tractorRepository.findByCompanyId(tenantGuard.scope());
        return tractorMapper.toResponses(tractors);
    }

    @Transactional
    public void delete(UUID id){
        tractorRepository.delete(findVisible(id));
    }

    /** Atualização completa aplicada sobre a entidade existente (preserva capacidades e auditoria). */
    @Transactional
    public TractorResponse update(UUID id, TractorCreateRequest request){
        Tractor tractor = findVisible(id);
        tractorValidate.validate(request, id);
        tractor.setPlate(request.plate());
        tractor.setRenavam(request.renavam());
        tractor.setModel(request.model());
        tractor.setNumberAxles(request.numberAxles());
        tractor.setMaximumCapacity(request.maximumCapacity());
        tractor.setTypeFuel(request.typeFuel());
        tractor.setCostPerKilometer(request.costPerKilometer());
        tractor.setStatus(request.status());
        tractor.setCompany(tenantGuard.ownerForWrite(request.companyId()));
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    @Transactional
    public TractorResponse updatePartial(UUID id, TractorUpdateRequest request){
        Tractor tractor = findVisible(id);
        tractorValidate.validate(request, id);

        if(request.plate() != null && !request.plate().isBlank())
            tractor.setPlate(request.plate());
        if(request.renavam() != null && !request.renavam().isBlank())
            tractor.setRenavam(request.renavam());
        if(request.model() != null && !request.model().isBlank())
            tractor.setModel(request.model());
        if(request.maximumCapacity() != null)
            tractor.setMaximumCapacity(request.maximumCapacity());
        if(request.numberAxles() != null)
            tractor.setNumberAxles(request.numberAxles());
        if(request.costPerKilometer() != null)
            tractor.setCostPerKilometer(request.costPerKilometer());
        if(request.companyId() != null)
            tractor.setCompany(tenantGuard.ownerForWrite(request.companyId()));

        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public Tractor findVisible(UUID id) {
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cavalo/caminhão não encontrado: ", id));
        tenantGuard.checkAccess(tractor.getCompany(), id);
        return tractor;
    }
}
