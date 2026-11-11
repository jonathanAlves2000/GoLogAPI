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
    private final GoLogAPI.repository.EquipamentGroupRepository equipamentGroupRepository;

    public TractorService(TractorRepository tractorRepository, TractorMapper tractorMapper,
                          TractorValidate tractorValidate, TenantGuard tenantGuard,
                          GoLogAPI.repository.EquipamentGroupRepository equipamentGroupRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
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
        applyDefaults(tractor);
        tractorRepository.save(tractor);
        ensureSingleVehicleGroup(tractor);
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
        if (request.vehicleType() != null) tractor.setVehicleType(request.vehicleType());
        tractor.setCargoVolume(request.cargoVolume());
        tractor.setKmPerLiter(request.kmPerLiter());
        tractor.setCo2PerKilometer(request.co2PerKilometer());
        applyDefaults(tractor);
        tractorRepository.save(tractor);
        ensureSingleVehicleGroup(tractor);
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
        if(request.vehicleType() != null)
            tractor.setVehicleType(request.vehicleType());
        if(request.cargoVolume() != null)
            tractor.setCargoVolume(request.cargoVolume());
        if(request.kmPerLiter() != null)
            tractor.setKmPerLiter(request.kmPerLiter());
        if(request.co2PerKilometer() != null)
            tractor.setCo2PerKilometer(request.co2PerKilometer());

        applyDefaults(tractor);
        tractorRepository.save(tractor);
        ensureSingleVehicleGroup(tractor);
        return tractorMapper.toResponse(tractor);
    }

    /** Sem valores presumidos: a emissão vem do cadastro ou do consumo informado (ver EmissionFactors). */
    private static void applyDefaults(Tractor tractor) {
        if (tractor.getKmPerLiter() != null && tractor.getKmPerLiter() <= 0) {
            throw new GoLogAPI.exception.BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "O consumo médio (km/L) deve ser maior que zero.");
        }
    }

    /**
     * Veículos rígidos (moto, van, VUC, truck...) operam sozinhos: garante um conjunto de veículo único para
     * que possam ser escalados e roteirizados sem passar pelo cadastro de conjuntos.
     */
    private void ensureSingleVehicleGroup(Tractor tractor) {
        if (tractor.getVehicleType() == null || !tractor.getVehicleType().isRigid()) return;
        boolean alreadyGrouped = equipamentGroupRepository.findAll().stream()
                .anyMatch(g -> (g.getEquipament1() != null && g.getEquipament1().getId().equals(tractor.getId()))
                        || (g.getEquipament2() != null && g.getEquipament2().getId().equals(tractor.getId()))
                        || (g.getEquipament3() != null && g.getEquipament3().getId().equals(tractor.getId())));
        if (alreadyGrouped) return;
        GoLogAPI.model.EquipamentGroup group = new GoLogAPI.model.EquipamentGroup();
        group.setEquipament1(tractor);
        group.setCompany(tractor.getCompany());
        group.setStatus(GoLogAPI.model.enums.EquipamentGroupStatus.DISPONIVEL);
        group.setTypeTransports(new java.util.HashSet<>());
        group.setObservation(tractor.getVehicleType().getDescription() + " " + tractor.getPlate() + " (veículo único)");
        equipamentGroupRepository.save(group);
    }

    public Tractor findVisible(UUID id) {
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cavalo/caminhão não encontrado: ", id));
        tenantGuard.checkAccess(tractor.getCompany(), id);
        return tractor;
    }
}
