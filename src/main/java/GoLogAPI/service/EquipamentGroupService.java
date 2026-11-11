package GoLogAPI.service;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupUpdateRequest;
import GoLogAPI.dto.supportedType.SupportedTypeCreateRequest;
import GoLogAPI.dto.supportedType.SupportedTypeResponse;
import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.EquipamentGroupMapper;
import GoLogAPI.mapper.SupportedTypeMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.model.enums.EquipamentGroupStatus;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.EquipamentRepository;
import GoLogAPI.repository.TypeTransportRepository;
import GoLogAPI.validation.EquipamentGroupValidate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Conjuntos de equipamentos (tração + até dois reboques). O conjunto pertence ao tenant dono da tração,
 * e todos os equipamentos precisam ser do mesmo tenant.
 */
@Service
@Transactional(readOnly = true)
public class EquipamentGroupService {

    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentGroupMapper equipamentGroupMapper;
    private final EquipamentRepository equipamentRepository;
    private final EquipamentGroupValidate equipamentGroupValidate;
    private final TypeTransportRepository typeTransportRepository;
    private final SupportedTypeMapper supportedTypeMapper;
    private final TenantGuard tenantGuard;

    public EquipamentGroupService(EquipamentGroupRepository equipamentGroupRepository,
                                  EquipamentGroupMapper equipamentGroupMapper, EquipamentRepository equipamentRepository,
                                  EquipamentGroupValidate equipamentGroupValidate, TypeTransportRepository typeTransportRepository,
                                  SupportedTypeMapper supportedTypeMapper, TenantGuard tenantGuard){
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentGroupMapper = equipamentGroupMapper;
        this.equipamentRepository = equipamentRepository;
        this.equipamentGroupValidate = equipamentGroupValidate;
        this.typeTransportRepository = typeTransportRepository;
        this.supportedTypeMapper = supportedTypeMapper;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public EquipamentGroupResponse save(EquipamentGroupCreateRequest request){
        equipamentGroupValidate.validate(request.equipament1Id(), request.equipament2Id(), request.equipament3Id(), null);
        EquipamentGroup group = new EquipamentGroup();
        group.setStatus(EquipamentGroupStatus.DISPONIVEL);
        group.setTypeTransports(new HashSet<>());
        applyComposition(group, request.observation(), request.equipament1Id(), request.equipament2Id(), request.equipament3Id());
        equipamentGroupRepository.save(group);
        return equipamentGroupMapper.toResponse(group);
    }

    public EquipamentGroupResponse get(UUID id){
        return equipamentGroupMapper.toResponse(findVisible(id));
    }

    public List<EquipamentGroupResponse> getAll() {
        List<EquipamentGroup> groups = tenantGuard.isGlobalView()
                ? equipamentGroupRepository.findAll()
                : equipamentGroupRepository.findByCompanyId(tenantGuard.scope());
        return equipamentGroupMapper.toResponses(groups);
    }

    @Transactional
    public void delete(UUID id){
        equipamentGroupRepository.delete(findVisible(id));
    }

    @Transactional
    public EquipamentGroupResponse update(UUID id, EquipamentGroupCreateRequest request){
        EquipamentGroup group = findVisible(id);
        equipamentGroupValidate.validate(request.equipament1Id(), request.equipament2Id(), request.equipament3Id(), id);
        applyComposition(group, request.observation(), request.equipament1Id(), request.equipament2Id(), request.equipament3Id());
        equipamentGroupRepository.save(group);
        return equipamentGroupMapper.toResponse(group);
    }

    @Transactional
    public EquipamentGroupResponse updatePartial(UUID id, EquipamentGroupUpdateRequest request){
        EquipamentGroup group = findVisible(id);
        UUID eq1 = request.equipament1Id() != null ? request.equipament1Id() : idOf(group.getEquipament1());
        UUID eq2 = request.equipament2Id() != null ? request.equipament2Id() : idOf(group.getEquipament2());
        UUID eq3 = request.equipament3Id() != null ? request.equipament3Id() : idOf(group.getEquipament3());
        equipamentGroupValidate.validate(eq1, eq2, eq3, id);
        String observation = request.observation() != null ? request.observation() : group.getObservation();
        applyComposition(group, observation, eq1, eq2, eq3);
        equipamentGroupRepository.save(group);
        return equipamentGroupMapper.toResponse(group);
    }

    @Transactional
    public SupportedTypeResponse addTransportType(SupportedTypeCreateRequest request){
        EquipamentGroup group = findVisible(request.groupId());
        TypeTransport typeTransport = typeTransportRepository.findById(request.typeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de transporte não encontrado: ", request.typeId()));
        tenantGuard.checkCatalogRead(typeTransport.getCompany(), request.typeId());
        if (group.getTypeTransports() == null) group.setTypeTransports(new HashSet<>());
        group.getTypeTransports().add(typeTransport);
        equipamentGroupRepository.save(group);
        return supportedTypeMapper.toResponse(group, typeTransport);
    }

    public EquipamentGroup findVisible(UUID id) {
        EquipamentGroup group = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conjunto não encontrado: ", id));
        tenantGuard.checkAccess(group.getCompany(), id);
        return group;
    }

    /** Aplica tração/reboques e herda o tenant da tração; reboques de outro tenant são rejeitados. */
    private void applyComposition(EquipamentGroup group, String observation, UUID eq1Id, UUID eq2Id, UUID eq3Id) {
        Equipament eq1 = findEquipament(eq1Id);
        Company owner = eq1.getCompany() != null ? eq1.getCompany() : tenantGuard.ownerForWrite(null);
        Equipament eq2 = eq2Id != null ? findEquipament(eq2Id) : null;
        Equipament eq3 = eq3Id != null ? findEquipament(eq3Id) : null;
        for (Equipament trailer : new Equipament[]{eq2, eq3}) {
            if (trailer != null && trailer.getCompany() != null && !Objects.equals(trailer.getCompany().getId(), owner.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "A carreta " + trailer.getPlate() + " pertence a outra empresa. Todos os equipamentos do conjunto devem ser do mesmo tenant.");
            }
        }
        group.setObservation(observation);
        group.setEquipament1(eq1);
        group.setEquipament2(eq2);
        group.setEquipament3(eq3);
        group.setCompany(owner);
        if (group.getStatus() == null) group.setStatus(EquipamentGroupStatus.DISPONIVEL);
    }

    private Equipament findEquipament(UUID id) {
        Equipament equipament = equipamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado: ", id));
        tenantGuard.checkAccess(equipament.getCompany(), id);
        return equipament;
    }

    private static UUID idOf(Equipament e) {
        return e != null ? e.getId() : null;
    }
}
