package GoLogAPI.service.telemetry;

import GoLogAPI.dto.telemetry.GeofenceEventResponse;
import GoLogAPI.dto.telemetry.TelemetryBatchItemRequest;
import GoLogAPI.dto.telemetry.TelemetryBatchResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Telemetry;
import GoLogAPI.model.Transport;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TelemetryBatchService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryBatchService.class);

    private final VehiclePlateCacheService vehiclePlateCacheService;
    private final GeofencingService geofencingService;
    private final TelemetryRepository telemetryRepository;
    private final TripTrackingService tripTrackingService;
    private final TelemetryTripLinkResolver tripLinkResolver;
    private final ShipmentRepository shipmentRepository;

    public TelemetryBatchService(VehiclePlateCacheService vehiclePlateCacheService,
                                 GeofencingService geofencingService,
                                 TelemetryRepository telemetryRepository,
                                 TripTrackingService tripTrackingService,
                                 TelemetryTripLinkResolver tripLinkResolver,
                                 ShipmentRepository shipmentRepository) {
        this.tripLinkResolver = tripLinkResolver;
        this.shipmentRepository = shipmentRepository;
        this.vehiclePlateCacheService = vehiclePlateCacheService;
        this.geofencingService = geofencingService;
        this.telemetryRepository = telemetryRepository;
        this.tripTrackingService = tripTrackingService;
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

        // Processa em ordem cronológica: a rota realizada é incremental e descarta pontos fora de ordem.
        // O índice original é mantido nas mensagens de erro.
        LocalDateTime receivedAt = LocalDateTime.now();
        List<Integer> order = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) order.add(i);
        order.sort(Comparator.comparing(i -> items.get(i).dateTime() != null ? items.get(i).dateTime() : receivedAt));

        for (int i : order) {
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

                LocalDateTime timestamp = item.dateTime() != null ? item.dateTime() : receivedAt;
                Double speed = item.speed() != null ? item.speed() : 0.0;
                String alert = item.alert();

                // 2. Rota realizada da viagem (antes do geofencing: o trecho que chega à parada ainda leva a carga dela)
                Transport runningTransport = tripTrackingService.track(equipament, lat, lon, timestamp);

                // 3. Normalização e Geofencing: detecção automática de chegada do caminhão na parada
                Optional<GeofenceEventResponse> geofenceEvent = geofencingService.checkGeofenceArrival(
                        equipament, lat, lon, timestamp, speed
                );

                if (geofenceEvent.isPresent()) {
                    GeofenceEventResponse event = geofenceEvent.get();
                    detectedEvents.add(event);

                    String checkInTag = "AUTO_CHECK_IN_STOP_" + event.sequenceOrder();
                    alert = (alert != null && !alert.isBlank()) ? (alert + " | " + checkInTag) : checkInTag;
                }

                // 4. Vínculo com a viagem: IDs explícitos do item têm prioridade; senão o transporte em execução
                //    do veículo e, se houve check-in, a remessa da parada atendida.
                Shipment shipment = null;
                Transport transport = runningTransport;
                if (item.shipmentId() != null || item.transportId() != null) {
                    TelemetryTripLinkResolver.TripLink link = tripLinkResolver.resolve(item.shipmentId(), item.transportId());
                    shipment = link.shipment();
                    transport = link.transport();
                } else if (geofenceEvent.isPresent() && geofenceEvent.get().shipmentId() != null) {
                    shipment = shipmentRepository.findById(geofenceEvent.get().shipmentId()).orElse(null);
                    if (transport == null && geofenceEvent.get().transportId() != null) {
                        transport = tripLinkResolver.resolve(null, geofenceEvent.get().transportId()).transport();
                    }
                }

                // 5. Montagem da entidade Telemetry
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
                telemetry.setShipmentId(shipment);
                telemetry.setTransportId(transport);

                telemetriesToSave.add(telemetry);
                successCount++;

            } catch (Exception e) {
                errorCount++;
                errors.add("Item #" + i + ": Erro no processamento - " + e.getMessage());
                log.warn("Erro ao processar item do batch de telemetria #{}: {}", i, e.getMessage());
            }
        }

        // 6. Ingestão em lote de alta performance no banco (saveAll)
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
