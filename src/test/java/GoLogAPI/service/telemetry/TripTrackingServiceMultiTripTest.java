package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TransportRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Rota realizada com o veículo tendo outra viagem, criada depois e ainda não iniciada (caso da viagem 23). */
class TripTrackingServiceMultiTripTest {

    private final TransportRepository transportRepository = mock(TransportRepository.class);
    private final RouteStopRepository routeStopRepository = mock(RouteStopRepository.class);
    private final TripTrackingService service =
            new TripTrackingService(transportRepository, new ActiveTripResolver(transportRepository, routeStopRepository));

    private static Transport trip() {
        Transport t = new Transport();
        t.setId(UUID.randomUUID());
        return t;
    }

    private void stops(Transport t, ShipmentStatus... statuses) {
        List<RouteStop> list = java.util.Arrays.stream(statuses).map(st -> {
            Shipment s = new Shipment();
            s.setStatus(st);
            s.setWeight(1000.0);
            RouteStop rs = new RouteStop();
            rs.setShipment(s);
            rs.setTransport(t);
            return rs;
        }).toList();
        when(routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(t.getId())).thenReturn(list);
    }

    @Test
    void pontosVaoParaViagemEmCursoENaoParaAFutura() {
        Equipament veiculo = new Equipament();
        veiculo.setId(UUID.randomUUID());
        Transport emCurso = trip();
        Transport futura = trip();
        stops(emCurso, ShipmentStatus.FINALIZADO, ShipmentStatus.AGUARDANDO_INICIO);
        stops(futura, ShipmentStatus.AGUARDANDO_INICIO);
        when(transportRepository.findByEquipament1IdOrderByCreatedAtDesc(veiculo.getId())).thenReturn(List.of(futura, emCurso));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 18, 16, 0);
        // App novo (com transportId) e app antigo (só veículo) chegam à mesma viagem
        assertEquals(emCurso, service.track(veiculo, emCurso, -22.35700, -47.38400, t0));
        assertEquals(emCurso, service.track(veiculo, null, -22.36000, -47.38000, t0.plusMinutes(5)));

        assertNotNull(emCurso.getRouteCompleted());
        assertTrue(emCurso.getDistanceTraveled() > 0);
        assertNull(futura.getRouteCompleted());
        verify(transportRepository, never()).save(futura);
    }
}
