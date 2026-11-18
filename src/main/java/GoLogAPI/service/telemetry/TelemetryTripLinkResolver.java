package GoLogAPI.service.telemetry;

import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.service.MessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Resolve e valida o vínculo opcional de uma leitura de telemetria com a remessa ({@code shipmentId}) e o
 * transporte ({@code transportId}) da viagem.
 *
 * <ul>
 *     <li>Ambos informados: a remessa precisa ter parada roteirizada naquele transporte.</li>
 *     <li>Só a remessa: o transporte é inferido pela parada roteirizada da remessa (se houver).</li>
 *     <li>Só o transporte: grava apenas o transporte.</li>
 * </ul>
 */
@Component
public class TelemetryTripLinkResolver {

    private final ShipmentRepository shipmentRepository;
    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;

    public TelemetryTripLinkResolver(ShipmentRepository shipmentRepository,
                                     TransportRepository transportRepository,
                                     RouteStopRepository routeStopRepository) {
        this.shipmentRepository = shipmentRepository;
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public record TripLink(Shipment shipment, Transport transport) {
        public static final TripLink EMPTY = new TripLink(null, null);
    }

    public TripLink resolve(UUID shipmentId, UUID transportId) {
        if (shipmentId == null && transportId == null) return TripLink.EMPTY;

        Shipment shipment = shipmentId == null ? null : shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentId));
        Transport transport = transportId == null ? null : transportRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, transportId));

        if (shipment == null) return new TripLink(null, transport);

        var stops = routeStopRepository.findByShipmentId(shipment.getId());
        if (transport == null) {
            Transport inferred = stops.stream()
                    .map(RouteStop::getTransport)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            return new TripLink(shipment, inferred);
        }

        boolean belongs = stops.stream()
                .anyMatch(rs -> rs.getTransport() != null && transport.getId().equals(rs.getTransport().getId()));
        if (!belongs) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "A remessa " + shipment.getId() + " não pertence ao transporte " + transport.getId() + ".");
        }
        return new TripLink(shipment, transport);
    }
}
