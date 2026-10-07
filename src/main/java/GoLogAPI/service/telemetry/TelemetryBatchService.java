package GoLogAPI.service.telemetry;

import GoLogAPI.dto.telemetry.GeofenceEventResponse;
import GoLogAPI.dto.telemetry.TelemetryBatchItemRequest;
import GoLogAPI.dto.telemetry.TelemetryBatchResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.Telemetry;
import GoLogAPI.repository.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TelemetryBatchService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryBatchService.class);

    private final VehiclePlateCacheService vehiclePlateCacheService;
    private final GeofencingService geofencingService;
    private final TelemetryRepository telemetryRepository;

    public TelemetryBatchService(VehiclePlateCacheService vehiclePlateCacheService,
                                 GeofencingService geofencingService,
                                 TelemetryRepository telemetryRepository) {
        this.vehiclePlateCacheService = vehiclePlateCacheService;
        this.geofencingService = geofencingService;
        this.telemetryRepository = telemetryRepository;
    }

    @Transactional
    public TelemetryBatchResponse processBatch(List<TelemetryBatchItemRequest> items) {
        long startTime = System.currentTimeMillis();

        if (items == null || items.isEmpty()) {
            return new TelemetryBatchResponse(0, 0, 0, List.of(), List.of(), 0L);
        }

        List<Telemetry> telemetriesToSave = new ArrayList<>(items.size());
        List<GeofenceEventResponse> detectedEvents = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < items.size(); i++) {
            TelemetryBatchItemRequest item = items.get(i);
            try {
                // 1. Resolução instantânea do equipamento via cache em memória (zero overhead de banco)
                Equipament equipament = vehiclePlateCacheService.resolveEquipament(item.plate(), item.equipamentId());
                if (equipament == null) {
                    errorCount++;
                    String identifier = item.plate() != null ? item.plate() : String.valueOf(item.equipamentId());
                    errors.add("Item #" + i + ": Veículo não encontrado para a placa/ID: " + identifier);
                    continue;
                }

                if (item.latitude() == null || item.longitude() == null) {
                    errorCount++;
                    errors.add("Item #" + i + ": Coordenadas geográficas inválidas ou nulas.");
                    continue;
                }

                String latStr = item.latitude().trim().replace(',', '.');
                String lonStr = item.longitude().trim().replace(',', '.');
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);

                LocalDateTime timestamp = item.dateTime() != null ? item.dateTime() : LocalDateTime.now();
                Double speed = item.speed() != null ? item.speed() : 0.0;
                String alert = item.alert();

                // 2. Normalização e Geofencing: detecção automática de chegada do caminhão na parada
                Optional<GeofenceEventResponse> geofenceEvent = geofencingService.checkGeofenceArrival(
                        equipament, lat, lon, timestamp, speed
                );

                if (geofenceEvent.isPresent()) {
                    GeofenceEventResponse event = geofenceEvent.get();
                    detectedEvents.add(event);

                    String checkInTag = "AUTO_CHECK_IN_STOP_" + event.sequenceOrder();
                    alert = (alert != null && !alert.isBlank()) ? (alert + " | " + checkInTag) : checkInTag;
                }

                // 3. Montagem da entidade Telemetry
                Telemetry telemetry = new Telemetry();
                telemetry.setEquipamentId(equipament);
                telemetry.setLatitude(latStr);
                telemetry.setLongitude(lonStr);
                telemetry.setSpeed(speed);
                telemetry.setDateTime(timestamp);
                telemetry.setAlert(alert);
                telemetry.setDevice(item.device());
                telemetry.setData1(item.data1());
                telemetry.setData2(item.data2());

                telemetriesToSave.add(telemetry);
                successCount++;

            } catch (Exception e) {
                errorCount++;
                errors.add("Item #" + i + ": Erro no processamento - " + e.getMessage());
                log.warn("Erro ao processar item do batch de telemetria #{}: {}", i, e.getMessage());
            }
        }

        // 4. Ingestão em lote de alta performance no banco (saveAll)
        if (!telemetriesToSave.isEmpty()) {
            telemetryRepository.saveAll(telemetriesToSave);
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Batch de telemetria processado: total={}, sucesso={}, erros={}, eventosGeofence={}, tempo={}ms",
                items.size(), successCount, errorCount, detectedEvents.size(), processingTime);

        return new TelemetryBatchResponse(
                items.size(),
                successCount,
                errorCount,
                errors,
                detectedEvents,
                processingTime
        );
    }
}
