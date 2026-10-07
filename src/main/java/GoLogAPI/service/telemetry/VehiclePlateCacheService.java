package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.repository.EquipamentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VehiclePlateCacheService {

    private static final Logger log = LoggerFactory.getLogger(VehiclePlateCacheService.class);

    private final EquipamentRepository equipamentRepository;

    // Cache Placa Normalizada -> ID do Equipamento
    private final Map<String, UUID> plateToIdCache = new ConcurrentHashMap<>();

    // Cache ID -> Instância do Equipamento
    private final Map<UUID, Equipament> equipamentCache = new ConcurrentHashMap<>();

    public VehiclePlateCacheService(EquipamentRepository equipamentRepository) {
        this.equipamentRepository = equipamentRepository;
    }

    @PostConstruct
    public void warmUpCache() {
        try {
            List<Equipament> allEquipaments = equipamentRepository.findAll();
            for (Equipament equipament : allEquipaments) {
                cacheEquipament(equipament);
            }
            log.info("VehiclePlateCache aquecido com sucesso! Total de veículos mapeados: {}", equipamentCache.size());
        } catch (Exception e) {
            log.warn("Não foi possível pré-aquecer o cache de veículos durante o startup: {}", e.getMessage());
        }
    }

    public Equipament resolveEquipament(String plate, UUID equipamentId) {
        if (equipamentId != null) {
            Equipament cached = equipamentCache.get(equipamentId);
            if (cached != null) {
                return cached;
            }
            // Fallback para banco
            return equipamentRepository.findById(equipamentId)
                    .map(this::cacheEquipament)
                    .orElse(null);
        }

        if (plate != null && !plate.isBlank()) {
            String cleanPlate = normalizePlate(plate);
            UUID id = plateToIdCache.get(cleanPlate);
            if (id != null) {
                Equipament cached = equipamentCache.get(id);
                if (cached != null) {
                    return cached;
                }
            }

            // Cache miss: busca no banco por placa com hífen ou sem hífen
            return equipamentRepository.findByPlate(plate.trim())
                    .or(() -> equipamentRepository.findByPlate(cleanPlate))
                    .map(this::cacheEquipament)
                    .orElse(null);
        }

        return null;
    }

    public Equipament cacheEquipament(Equipament equipament) {
        if (equipament == null || equipament.getId() == null) {
            return equipament;
        }

        equipamentCache.put(equipament.getId(), equipament);

        if (equipament.getPlate() != null && !equipament.getPlate().isBlank()) {
            String normalized = normalizePlate(equipament.getPlate());
            plateToIdCache.put(normalized, equipament.getId());
            plateToIdCache.put(equipament.getPlate().trim().toUpperCase(), equipament.getId());
        }

        return equipament;
    }

    public void evict(String plate, UUID id) {
        if (id != null) {
            equipamentCache.remove(id);
        }
        if (plate != null) {
            plateToIdCache.remove(normalizePlate(plate));
            plateToIdCache.remove(plate.trim().toUpperCase());
        }
    }

    public void clear() {
        plateToIdCache.clear();
        equipamentCache.clear();
    }

    public int getCacheSize() {
        return equipamentCache.size();
    }

    public static String normalizePlate(String rawPlate) {
        if (rawPlate == null) return "";
        return rawPlate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
