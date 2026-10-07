package GoLogAPI.service.telemetry;

import GoLogAPI.dto.telemetry.GeofenceEventResponse;
import GoLogAPI.model.Address;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.RouteStopStatus;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TransportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeofencingService {

    private static final Logger log = LoggerFactory.getLogger(GeofencingService.class);

    // Raio padrão para detecção de chegada na parada (200 metros)
    public static final double DEFAULT_GEOFENCE_RADIUS_METERS = 200.0;

    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;
    private final ShipmentRepository shipmentRepository;

    public GeofencingService(TransportRepository transportRepository,
                             RouteStopRepository routeStopRepository,
                             ShipmentRepository shipmentRepository) {
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Processa as coordenadas de um veículo e verifica se ele adentrou o raio de alguma parada pendente.
     * Caso positivo, executa o check-in automático.
     */
    @Transactional
    public Optional<GeofenceEventResponse> checkGeofenceArrival(
            Equipament equipament,
            double vehicleLat,
            double vehicleLon,
            LocalDateTime timestamp,
            Double speed) {

        if (equipament == null || equipament.getId() == null) {
            return Optional.empty();
        }

        // 1. Busca os transportes do veículo
        List<Transport> transports = transportRepository.findByEquipament1IdOrderByCreatedAtDesc(equipament.getId());
        if (transports.isEmpty() && equipament.getPlate() != null) {
            transports = transportRepository.findByEquipament1PlateOrderByCreatedAtDesc(equipament.getPlate());
        }

        if (transports.isEmpty()) {
            return Optional.empty();
        }

        // Transporte mais recente / em andamento
        Transport currentTransport = transports.get(0);

        // 2. Busca paradas pendentes que ainda não tiveram chegada
        List<RouteStop> pendingStops = routeStopRepository.findByTransportIdAndArrivedAtIsNullOrderBySequenceOrderAsc(currentTransport.getId());
        if (pendingStops.isEmpty()) {
            return Optional.empty();
        }

        // 3. Avalia cada parada pendente
        for (RouteStop stop : pendingStops) {
            Shipment shipment = stop.getShipment();
            if (shipment == null || shipment.getAddress() == null) {
                continue;
            }

            Address addr = shipment.getAddress();
            Double targetLat = parseCoordinate(addr.getLatitude());
            Double targetLon = parseCoordinate(addr.getLongitude());

            if (targetLat == null || targetLon == null) {
                continue;
            }

            double distance = calculateHaversineDistanceMeters(vehicleLat, vehicleLon, targetLat, targetLon);

            if (distance <= DEFAULT_GEOFENCE_RADIUS_METERS) {
                LocalDateTime arrivalTime = timestamp != null ? timestamp : LocalDateTime.now();

                stop.setArrivedAt(arrivalTime);
                stop.setAutoCheckIn(true);
                stop.setCheckInDistance(Math.round(distance * 100.0) / 100.0);
                stop.setStatus(RouteStopStatus.CHEGOU);

                if (shipment.getStatus() == ShipmentStatus.AGUARDANDO_INICIO || shipment.getStatus() == ShipmentStatus.PENDENTE) {
                    shipment.setStatus(ShipmentStatus.INICIADO);
                    shipmentRepository.save(shipment);
                }

                routeStopRepository.save(stop);

                String plate = equipament.getPlate() != null ? equipament.getPlate() : "N/A";
                log.info("GEOFENCE CHECK-IN AUTOMÁTICO: Veículo {} atingiu parada #{} (Distância: {}m)",
                        plate, stop.getSequenceOrder(), Math.round(distance));

                GeofenceEventResponse event = new GeofenceEventResponse(
                        "AUTO_CHECK_IN",
                        stop.getId(),
                        shipment.getId(),
                        currentTransport.getId(),
                        plate,
                        stop.getSequenceOrder(),
                        Math.round(distance * 100.0) / 100.0,
                        RouteStopStatus.CHEGOU,
                        arrivalTime,
                        String.format("Chegada detectada a %.1fm da parada #%d. Check-in automático efetuado.", distance, stop.getSequenceOrder())
                );

                return Optional.of(event);
            }
        }

        return Optional.empty();
    }

    /**
     * Calcula distância geodésica em metros utilizando a fórmula de Haversine.
     */
    public static double calculateHaversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // Raio da Terra em metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static Double parseCoordinate(String coord) {
        if (coord == null || coord.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(coord.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
