package GoLogAPI.service;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.repository.EquipamentGroupRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Completa respostas da frota com o conjunto do equipamento e os tipos de transporte que ele atende. */
@Component
public class FleetGroupLookup {

    private final EquipamentGroupRepository equipamentGroupRepository;

    public FleetGroupLookup(EquipamentGroupRepository equipamentGroupRepository) {
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    public <T extends EquipamentResponse> T enrich(T response) {
        if (response != null) enrichAll(java.util.List.of(response));
        return response;
    }

    public void enrichAll(Collection<? extends EquipamentResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Map<UUID, EquipamentGroup> byEquipament = new HashMap<>();
        for (EquipamentGroup g : equipamentGroupRepository.findAll()) {
            for (Equipament e : new Equipament[]{g.getEquipament1(), g.getEquipament2(), g.getEquipament3()}) {
                if (e != null) byEquipament.put(e.getId(), g);
            }
        }
        for (EquipamentResponse r : responses) {
            EquipamentGroup g = r == null ? null : byEquipament.get(r.getId());
            if (g == null) continue;
            r.setEquipamentGroupId(g.getId());
            var types = g.getTypeTransports() == null ? java.util.List.<TypeTransport>of()
                    : g.getTypeTransports().stream().sorted(Comparator.comparing(TypeTransport::getName)).toList();
            r.setTypeTransportIds(types.stream().map(TypeTransport::getId).toList());
            r.setTypeTransportNames(types.stream().map(TypeTransport::getName).toList());
        }
    }
}
