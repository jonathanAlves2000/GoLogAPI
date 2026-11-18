package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.Telemetry;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TelemetryRepository;
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
    private final TelemetryRepository telemetryRepository = mock(TelemetryRepository.class);
    private final TripTrackingService service = new TripTrackingService(transportRepository,
            new ActiveTripResolver(transportRepository, routeStopRepository), telemetryRepository);

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

    private static Telemetry point(Transport t, String lat, String lon, LocalDateTime at) {
        Telemetry p = new Telemetry();
        p.setTransportId(t);
        p.setLatitude(lat);
        p.setLongitude(lon);
        p.setDateTime(at);
        return p;
    }

    @Test
    void rebuildMontaRotaSoComPontosDoTransportIdESubstituiRotaErrada() {
        Transport viagem = trip();
        viagem.setRouteCompleted("rota_errada");
        viagem.setDistanceTraveled(999_999);
        LocalDateTime t0 = LocalDateTime.of(2026, 11, 18, 16, 0);
        when(telemetryRepository.findByTransportIdIdOrderByDateTimeAsc(viagem.getId())).thenReturn(List.of(
                point(viagem, "-22.35700", "-47.38400", t0),
                point(viagem, "-22,36000", "-47,38000", t0.plusMinutes(5)),   // vírgula decimal
                point(viagem, "invalido", "-47.38000", t0.plusMinutes(6)),   // coordenada inválida é ignorada
                point(viagem, "-22.36500", "-47.37000", t0.plusMinutes(10))));

        assertTrue(service.rebuild(viagem, List.of()));
        assertNotEquals("rota_errada", viagem.getRouteCompleted());
        assertTrue(viagem.getDistanceTraveled() > 1000 && viagem.getDistanceTraveled() < 3000);
        assertEquals(t0, viagem.getTrackingStartedAt());
        assertEquals(t0.plusMinutes(10), viagem.getLastTelemetryAt());

        // Idempotente: remontar de novo não muda nada
        assertFalse(service.rebuild(viagem, List.of()));
    }

    @Test
    void rebuildSemPontosLimpaARota() {
        Transport viagem = trip();
        viagem.setRouteCompleted("rota_errada");
        when(telemetryRepository.findByTransportIdIdOrderByDateTimeAsc(viagem.getId())).thenReturn(List.of());
        assertTrue(service.rebuild(viagem, List.of()));
        assertNull(viagem.getRouteCompleted());
        assertNull(viagem.getTrackingStartedAt());
    }
}
