package GoLogAPI.service;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.EquipamentMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.repository.EquipamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EquipamentService {

    private final EquipamentRepository equipamentRepository;
    private final EquipamentMapper equipamentMapper;

    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;
    private FleetGroupLookup fleetGroupLookup;

    @org.springframework.beans.factory.annotation.Autowired
    void setFleetGroupLookup(FleetGroupLookup fleetGroupLookup) { this.fleetGroupLookup = fleetGroupLookup; }

    public EquipamentService(EquipamentRepository equipamentRepository, EquipamentMapper equipamentMapper,
                             GoLogAPI.infra.tenant.TenantGuard tenantGuard){
        this.equipamentRepository = equipamentRepository;
        this.equipamentMapper = equipamentMapper;
        this.tenantGuard = tenantGuard;
    }

    public EquipamentResponse get(UUID id){
        Equipament equipament = equipamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkAccess(equipament.getCompany(), id);
        return fleetGroupLookup.enrich(equipamentMapper.toResponse(equipament));
    }

    public List<EquipamentResponse> getAll(){
        List<Equipament> equipaments = tenantGuard.isGlobalView()
                ? equipamentRepository.findAll()
                : equipamentRepository.findByCompanyId(tenantGuard.scope());
        List<EquipamentResponse> responses = equipamentMapper.toResponseList(equipaments);
        fleetGroupLookup.enrichAll(responses);
        return responses;
    }
}
