package GoLogAPI.service.routeOptimization;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.TypeOperation;
import com.google.maps.routeoptimization.v1.ShipmentRoute;
import com.google.protobuf.Duration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Coleta com 3 entregas filhas: o Google devolve 3 visitas de coleta seguidas (caso da viagem 29). */
class ProcessRouteStopServiceTest {

    private static Shipment shipment(TypeOperation op) {
        Shipment s = new Shipment();
        s.setId(UUID.randomUUID());
        s.setTypeOperation(op);
        return s;
    }

    private static ShipmentRoute.Visit visit(String label, boolean pickup) {
        return ShipmentRoute.Visit.newBuilder().setShipmentLabel(label).setIsPickup(pickup).build();
    }

    private static ShipmentRoute.Transition leg(long meters, long seconds) {
        return ShipmentRoute.Transition.newBuilder()
                .setTravelDistanceMeters(meters)
                .setTotalDuration(Duration.newBuilder().setSeconds(seconds))
                .setWaitDuration(Duration.newBuilder().setSeconds(0))
                .build();
    }

    @Test
    void coletaComTresFilhasViraUmaParadaSo() {
        Shipment coleta = shipment(TypeOperation.COLETA);
        Shipment e1 = shipment(TypeOperation.ENTREGA);
        Shipment e2 = shipment(TypeOperation.ENTREGA);
        Shipment e3 = shipment(TypeOperation.ENTREGA);
        String c = coleta.getId().toString();

        List<ShipmentRoute.Visit> visits = List.of(
                visit(c + "/" + e1.getId(), true),
                visit(c + "/" + e2.getId(), true),
                visit(c + "/" + e3.getId(), true),
                visit(c + "/" + e1.getId(), false),
                visit(c + "/" + e2.getId(), false),
                visit(c + "/" + e3.getId(), false));
        List<ShipmentRoute.Transition> legs = List.of(
                leg(1200, 300), leg(0, 60), leg(0, 60), leg(900, 240), leg(700, 200), leg(500, 150));

        List<UUID> ids = new ArrayList<>();
        visits.forEach(v -> ids.add(ProcessRouteStopService.resolveTargetId(v)));
        Map<UUID, Shipment> byId = new HashMap<>();
        for (Shipment s : List.of(coleta, e1, e2, e3)) byId.put(s.getId(), s);

        List<RouteStop> stops = ProcessRouteStopService.buildStops(visits, legs, ids, byId, new Transport());

        assertEquals(4, stops.size());
        assertEquals(List.of(coleta, e1, e2, e3), stops.stream().map(RouteStop::getShipment).toList());
        assertEquals(List.of(1, 2, 3, 4), stops.stream().map(RouteStop::getSequenceOrder).toList());
        // Trechos das visitas juntadas somam na parada da coleta
        assertEquals(1200, stops.get(0).getCalculatedDistance());
        assertEquals(420, stops.get(0).getCalculatedDuration());
    }

    @Test
    void mesmaRemessaNaoSeguidaContinuaSeparada() {
        Shipment a = shipment(TypeOperation.ENTREGA);
        Shipment b = shipment(TypeOperation.ENTREGA);
        List<ShipmentRoute.Visit> visits = List.of(
                visit("delivery/" + a.getId(), false), visit("delivery/" + b.getId(), false), visit("delivery/" + a.getId(), false));
        List<UUID> ids = new ArrayList<>();
        visits.forEach(v -> ids.add(ProcessRouteStopService.resolveTargetId(v)));
        List<RouteStop> stops = ProcessRouteStopService.buildStops(visits, List.of(leg(1, 1), leg(1, 1), leg(1, 1)), ids,
                Map.of(a.getId(), a, b.getId(), b), new Transport());
        assertEquals(3, stops.size());
    }
}
