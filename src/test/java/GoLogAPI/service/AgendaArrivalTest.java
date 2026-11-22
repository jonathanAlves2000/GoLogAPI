package GoLogAPI.service;

import GoLogAPI.model.Occurrence;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgendaArrivalTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 11, 22, 9, 0);

    private static Shipment shipment() {
        Shipment s = new Shipment();
        s.setId(UUID.randomUUID());
        return s;
    }

    private static RouteStop stop(Shipment s, LocalDateTime arrived) {
        RouteStop rs = new RouteStop();
        rs.setShipment(s);
        rs.setArrivedAt(arrived);
        return rs;
    }

    private static Occurrence occ(Shipment s, String type, LocalDateTime at) {
        Occurrence o = new Occurrence();
        o.setShipment(s);
        o.setType(type);
        o.setDateTime(at);
        return o;
    }

    @Test
    void geofenceTemPrioridadeSobreRegistroDeEntrega() {
        Shipment s = shipment();
        Map<UUID, AgendaArrival.Arrival> r = AgendaArrival.resolve(List.of(stop(s, T.plusMinutes(5))),
                List.of(occ(s, "Fim", T.minusMinutes(30))));
        assertEquals(AgendaArrival.Source.GEOFENCE, r.get(s.getId()).source());
        assertEquals(T.plusMinutes(5), r.get(s.getId()).at());
    }

    @Test
    void semGeofenceUsaOPrimeiroFimEIgnoraOutrosTipos() {
        Shipment s = shipment();
        Map<UUID, AgendaArrival.Arrival> r = AgendaArrival.resolve(List.of(stop(s, null)), List.of(
                occ(s, "Inicio", T.minusHours(1)),
                occ(s, "Fim", T.plusMinutes(2)),
                occ(s, " fim ", T.minusMinutes(3))));
        assertEquals(AgendaArrival.Source.DELIVERY_RECORD, r.get(s.getId()).source());
        assertEquals(T.minusMinutes(3), r.get(s.getId()).at());
    }

    @Test
    void semNenhumRegistroNaoHaChegada() {
        Shipment s = shipment();
        assertFalse(AgendaArrival.resolve(List.of(stop(s, null)), List.of(occ(s, "Parada", T))).containsKey(s.getId()));
    }

    @Test
    void chegadaAntecipadaEstaNoPrazo() {
        assertTrue(new AgendaArrival.Arrival(T.minusHours(3), AgendaArrival.Source.GEOFENCE).onTime(T));
        assertTrue(new AgendaArrival.Arrival(T, AgendaArrival.Source.GEOFENCE).onTime(T));
        assertFalse(new AgendaArrival.Arrival(T.plusMinutes(1), AgendaArrival.Source.GEOFENCE).onTime(T));
    }
}
