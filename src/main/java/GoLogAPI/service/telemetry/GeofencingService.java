package GoLogAPI.service.telemetry;

import GoLogAPI.dto.telemetry.GeofenceEventResponse;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.OptimizationProfile;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.RouteStopStatus;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.OptimizationProfileRepository;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GeofencingService {

    private static final Logger log = LoggerFactory.getLogger(GeofencingService.class);

    // Raio padrão de chegada na parada (100 m) quando nem o cliente nem o perfil da transportadora definem outro.
    // Basta um ponto de telemetria dentro do raio, sem critério de velocidade.
    public static final double DEFAULT_GEOFENCE_RADIUS_METERS = 100.0;

    private final RouteStopRepository routeStopRepository;
    private final ShipmentRepository shipmentRepository;
    private final GoLogAPI.service.webhook.WebhookDispatcherService webhookDispatcherService;
    private final ActiveTripResolver tripResolver;
    private final OptimizationProfileRepository optimizationProfileRepository;

    public GeofencingService(RouteStopRepository routeStopRepository,
                             ShipmentRepository shipmentRepository,
                             GoLogAPI.service.webhook.WebhookDispatcherService webhookDispatcherService,
                             ActiveTripResolver tripResolver,
                             OptimizationProfileRepository optimizationProfileRepository) {
        this.routeStopRepository = routeStopRepository;
        this.shipmentRepository = shipmentRepository;
        this.webhookDispatcherService = webhookDispatcherService;
        this.tripResolver = tripResolver;
        this.optimizationProfileRepository = optimizationProfileRepository;
    }

    /**
     * Raio da parada: o do cliente da remessa, se definido; senão o do perfil de otimização padrão da
     * transportadora; senão {@value #DEFAULT_GEOFENCE_RADIUS_METERS} m. Nunca abaixo de 100 m (mínimo do cadastro).
     */
    static double radiusMeters(Company customer, Integer transporterDefault) {
        double min = GoLogAPI.service.routeOptimization.OptimizationDefaults.MIN_GEOFENCE_RADIUS_METERS;
        if (customer != null && customer.getGeofenceRadiusMeters() != null && customer.getGeofenceRadiusMeters() > 0) {
            return Math.max(min, customer.getGeofenceRadiusMeters());
        }
        if (transporterDefault != null && transporterDefault > 0) return Math.max(min, transporterDefault);
        return DEFAULT_GEOFENCE_RADIUS_METERS;
    }

    private Integer transporterDefaultRadius(Transport transport) {
        if (optimizationProfileRepository == null || transport.getTransporter() == null
                || transport.getTransporter().getId() == null) return null;
        UUID companyId = transport.getTransporter().getId();
        return optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(companyId)
                .or(() -> optimizationProfileRepository.findFirstByCompanyId(companyId))
                .map(OptimizationProfile::getGeofenceRadiusMeters)
                .orElse(null);
    }

    /** Check-in automático na viagem do veículo, sem viagem informada no ponto. */
    @Transactional
    public Optional<GeofenceEventResponse> checkGeofenceArrival(
            Equipament equipament,
            double vehicleLat,
            double vehicleLon,
            LocalDateTime timestamp,
            Double speed) {
        return checkGeofenceArrival(equipament, null, vehicleLat, vehicleLon, timestamp, speed);
    }

    /**
     * Processa as coordenadas de um veículo e verifica se ele adentrou o raio de alguma parada pendente.
     * Caso positivo, executa o check-in automático.
     *
     * <p>A viagem é a informada pelo app no ponto ({@code linked}) ou, sem ela, a em execução do veículo; se
     * nenhuma estiver em execução, a aberta mais recente, para o check-in na primeira parada iniciar a viagem
     * (critério em {@link ActiveTripResolver}).</p>
     */
    @Transactional
    public Optional<GeofenceEventResponse> checkGeofenceArrival(
            Equipament equipament,
            Transport linked,
            double vehicleLat,
            double vehicleLon,
            LocalDateTime timestamp,
            Double speed) {

        // 1. Viagem à qual o ponto pertence
        ActiveTripResolver.ResolvedTrip trip = tripResolver.resolve(equipament, linked, true);
        if (trip == null) {
            return Optional.empty();
        }
        Transport currentTransport = trip.transport();

        // 2. Busca paradas pendentes que ainda não tiveram chegada
        List<RouteStop> pendingStops = routeStopRepository.findByTransportIdAndArrivedAtIsNullOrderBySequenceOrderAsc(currentTransport.getId());
        if (pendingStops.isEmpty()) {
            return Optional.empty();
        }

        // 3. Avalia cada parada pendente
        Integer transporterRadius = transporterDefaultRadius(currentTransport);
        for (RouteStop stop : pendingStops) {
            Shipment shipment = stop.getShipment();
            if (shipment == null) {
                continue;
            }

            Address addr = shipment.getAddress() != null ? shipment.getAddress()
                    : (shipment.getCustomer() != null ? shipment.getCustomer().getAddress() : null);
            if (addr == null) {
                continue;
            }

            Double targetLat = parseCoordinate(addr.getLatitude());
            Double targetLon = parseCoordinate(addr.getLongitude());

            if (targetLat == null || targetLon == null) {
                continue;
            }

            double distance = calculateHaversineDistanceMeters(vehicleLat, vehicleLon, targetLat, targetLon);

            if (distance <= radiusMeters(shipment.getCustomer(), transporterRadius)) {
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

                // Dispara notificação assíncrona para o webhook cadastrado no ERP do cliente/transportador
                Company notifyingCompany = currentTransport.getTransporter() != null
                        ? currentTransport.getTransporter()
                        : (shipment.getCompany() != null ? shipment.getCompany() : null);

                if (notifyingCompany != null && webhookDispatcherService != null) {
                    webhookDispatcherService.dispatch(notifyingCompany, "CHECK_IN_GEOFENCE", event);
                }

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
