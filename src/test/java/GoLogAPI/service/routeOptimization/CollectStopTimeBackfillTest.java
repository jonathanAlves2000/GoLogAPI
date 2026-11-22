package GoLogAPI.service.routeOptimization;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.TypeOperation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CollectStopTimeBackfillTest {

    private static Shipment shipment(TypeOperation op, Shipment origem) {
        Shipment s = new Shipment();
        s.setId(UUID.randomUUID());
        s.setTypeOperation(op);
        s.setOperationOrigem(origem);
        return s;
    }

    private static RouteStop stop(int seq, Shipment s, int meters) {
        RouteStop rs = new RouteStop();
        rs.setSequenceOrder(seq);
        rs.setShipment(s);
        rs.setCalculatedDistance(meters);
        rs.setCalculatedDuration(0);
        rs.setCalculatedWait(0);
        return rs;
    }

    private static Transport trip(int totalTime, int travel, double costHour, double totalCost) {
        Transport t = new Transport();
        t.setTotalTimeCalculed(totalTime);
        t.setTravelDuration(travel);
        t.setCostHourCalculed(costHour);
        t.setTotalCostCalculed(totalCost);
        return t;
    }

    /** Viagem 29: 1 coleta (3 filhas) repetida em 3 paradas + 3 entregas, 15 min por parada. */
    @Test
    void viagem29() {
        Shipment coleta = shipment(TypeOperation.COLETA, null);
        Shipment e1 = shipment(TypeOperation.ENTREGA, coleta);
        Shipment e2 = shipment(TypeOperation.ENTREGA, coleta);
        Shipment e3 = shipment(TypeOperation.ENTREGA, coleta);
        RouteStop c1 = stop(1, coleta, 1200);
        RouteStop c2 = stop(2, coleta, 0);
        RouteStop c3 = stop(3, coleta, 0);
        c3.setArrivedAt(LocalDateTime.of(2026, 11, 19, 10, 15));
        List<RouteStop> stops = new ArrayList<>(List.of(c1, c2, c3, stop(4, e1, 900), stop(5, e2, 700), stop(6, e3, 500)));

        Transport t = trip(6417, 1017, 26.7375, 88.80394);
        CollectStopTimeBackfill.Fix fix = CollectStopTimeBackfill.compute(t, stops);

        assertEquals(4, fix.keep().size());
        assertEquals(2, fix.remove().size());
        assertEquals(2, fix.extraVisits());
        assertEquals(4617, fix.newTotalTime());                 // 1h17 = 1017 s + 4 × 900 s
        assertEquals(19.2375, fix.newCostHour(), 0.001);        // mesma tarifa de R$ 15/h
        assertEquals(81.30, fix.newTotalCost(), 0.01);
        assertEquals(4, fix.shipmentQuantity());
        assertEquals(List.of(1, 2, 3, 4), fix.keep().stream().map(RouteStop::getSequenceOrder).toList());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 15), fix.keep().get(0).getArrivedAt()); // chegada preservada
    }

    /** Viagem 17: 10 paradas, 4 repetidas, 10 min por parada (perfil diferente) — duração derivada do próprio plano. */
    @Test
    void viagem17DuracaoDerivadaDoPlano() {
        Shipment coleta = shipment(TypeOperation.COLETA, null);
        List<RouteStop> stops = new ArrayList<>();
        int seq = 1;
        for (int i = 0; i < 5; i++) stops.add(stop(seq++, coleta, i == 0 ? 1000 : 0));
        for (int i = 0; i < 5; i++) stops.add(stop(seq++, shipment(TypeOperation.ENTREGA, coleta), 800));

        Transport t = trip(16131, 10131, 67.21, 136.27);
        CollectStopTimeBackfill.Fix fix = CollectStopTimeBackfill.compute(t, stops);

        assertEquals(4, fix.extraVisits());
        assertEquals(16131 - 4 * 600, fix.newTotalTime());
        assertEquals(126.27, fix.newTotalCost(), 0.01);
    }

    /** Paradas já juntas (correção da tela publicada antes): ainda corrige o tempo inflado. */
    @Test
    void paradasJaJuntasAindaCorrigeTempo() {
        Shipment coleta = shipment(TypeOperation.COLETA, null);
        List<RouteStop> stops = new ArrayList<>(List.of(stop(1, coleta, 1200),
                stop(2, shipment(TypeOperation.ENTREGA, coleta), 900),
                stop(3, shipment(TypeOperation.ENTREGA, coleta), 700),
                stop(4, shipment(TypeOperation.ENTREGA, coleta), 500)));
        CollectStopTimeBackfill.Fix fix = CollectStopTimeBackfill.compute(trip(6417, 1017, 26.7375, 88.80394), stops);
        assertTrue(fix.remove().isEmpty());
        assertEquals(4617, fix.newTotalTime());
    }

    @Test
    void viagemSemColetaComFilhasNaoMuda() {
        Shipment coleta = shipment(TypeOperation.COLETA, null);
        List<RouteStop> stops = new ArrayList<>(List.of(stop(1, coleta, 100),
                stop(2, shipment(TypeOperation.ENTREGA, coleta), 100),
                stop(3, shipment(TypeOperation.ENTREGA, null), 100)));
        Transport t = trip(4000, 1300, 20.0, 80.0);
        CollectStopTimeBackfill.Fix fix = CollectStopTimeBackfill.compute(t, stops);
        assertEquals(0, fix.extraVisits());
        assertFalse(fix.changesSomething(t));
        assertEquals(4000, fix.newTotalTime());
    }
}
