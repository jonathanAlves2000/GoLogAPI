package GoLogAPI.service.telemetry;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TripTrackingServiceTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 17, 8, 0);

    private static RouteStop stop(TypeOperation op, double kg, ShipmentStatus status, LocalDateTime arrived) {
        Shipment s = new Shipment();
        s.setId(UUID.randomUUID());
        s.setTypeOperation(op);
        s.setWeight(kg);
        s.setStatus(status);
        RouteStop rs = new RouteStop();
        rs.setShipment(s);
        rs.setArrivedAt(arrived);
        return rs;
    }

    @Test
    void polilinhaIncrementalIgualACodificacaoDaListaInteira() {
        Transport t = new Transport();
        List<RouteStop> stops = List.of(stop(TypeOperation.ENTREGA, 2000, ShipmentStatus.INICIADO, null));
        double[][] pts = {{-22.35700, -47.38400}, {-22.36000, -47.38000}, {-22.36500, -47.37000}};
        for (int i = 0; i < pts.length; i++) {
            assertTrue(TripTrackingService.apply(t, stops, pts[i][0], pts[i][1], T0.plusMinutes(i * 5L)));
        }
        String expected = PolylineEncoding.encode(List.of(
                new LatLng(pts[0][0], pts[0][1]), new LatLng(pts[1][0], pts[1][1]), new LatLng(pts[2][0], pts[2][1])));
        assertEquals(expected, t.getRouteCompleted());
        assertTrue(t.getDistanceTraveled() > 1000);
        // 2 t a bordo durante todo o trajeto
        assertEquals(2.0 * t.getDistanceTraveled() / 1000.0, t.getTonKmTraveled(), 0.01);
        assertEquals(T0, t.getTrackingStartedAt());
    }

    @Test
    void descartaRuidoPontoForaDeOrdemESaltoImpossivel() {
        Transport t = new Transport();
        List<RouteStop> stops = List.of(stop(TypeOperation.ENTREGA, 1000, ShipmentStatus.INICIADO, null));
        TripTrackingService.apply(t, stops, -22.35700, -47.38400, T0);
        assertFalse(TripTrackingService.apply(t, stops, -22.35705, -47.38405, T0.plusMinutes(1)));   // ~7 m
        assertFalse(TripTrackingService.apply(t, stops, -22.36000, -47.38000, T0.minusMinutes(1)));  // fora de ordem
        assertFalse(TripTrackingService.apply(t, stops, -23.55000, -46.63000, T0.plusMinutes(2)));   // ~150 km em 2 min
        assertEquals(0, t.getDistanceTraveled());
    }

    @Test
    void cargaABordoDescontaEntregasAtendidasESomaColetas() {
        RouteStop e1 = stop(TypeOperation.ENTREGA, 1500, ShipmentStatus.FINALIZADO, T0);
        RouteStop e2 = stop(TypeOperation.ENTREGA, 500, ShipmentStatus.INICIADO, null);
        RouteStop c1 = stop(TypeOperation.COLETA, 800, ShipmentStatus.FINALIZADO, T0.plusMinutes(10));
        assertEquals(500 + 800, TripLoad.onboardKg(List.of(e1, e2, c1)), 0.001);
    }

    @Test
    void soAcumulaComViagemEmExecucao() {
        assertFalse(TripTrackingService.isRunning(List.of(stop(TypeOperation.ENTREGA, 1, ShipmentStatus.AGUARDANDO_INICIO, null))));
        assertTrue(TripTrackingService.isRunning(List.of(stop(TypeOperation.ENTREGA, 1, ShipmentStatus.INICIADO, null))));
        assertFalse(TripTrackingService.isRunning(List.of(stop(TypeOperation.ENTREGA, 1, ShipmentStatus.FINALIZADO, T0))));
    }

    @Test
    void cargaNoInstanteSoDescontaParadasAtendidasAteEle() {
        RouteStop e1 = stop(TypeOperation.ENTREGA, 1500, ShipmentStatus.FINALIZADO, T0.plusMinutes(30));
        RouteStop e2 = stop(TypeOperation.ENTREGA, 500, ShipmentStatus.AGUARDANDO_INICIO, null);
        List<RouteStop> stops = List.of(e1, e2);
        assertEquals(2000, TripLoad.onboardKgAt(stops, T0), 0.001);                  // antes da 1ª entrega
        assertEquals(500, TripLoad.onboardKgAt(stops, T0.plusMinutes(31)), 0.001);   // depois dela
    }
}
