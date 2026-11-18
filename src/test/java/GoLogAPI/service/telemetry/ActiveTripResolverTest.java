package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TransportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Veículo com três viagens (criadas nesta ordem): concluída, em curso e futura. Reproduz o caso da viagem 23,
 * cujos pontos iam para a viagem futura (a mais recente) e eram descartados.
 */
class ActiveTripResolverTest {

    private final TransportRepository transportRepository = mock(TransportRepository.class);
    private final RouteStopRepository routeStopRepository = mock(RouteStopRepository.class);
    private final ActiveTripResolver resolver = new ActiveTripResolver(transportRepository, routeStopRepository);
    private final Map<UUID, List<RouteStop>> stopsByTransport = new HashMap<>();

    private final Equipament veiculo = new Equipament();
    private Transport concluida;
    private Transport emCurso;
    private Transport futura;

    @BeforeEach
    void setUp() {
        veiculo.setId(UUID.randomUUID());
        concluida = transport(ShipmentStatus.FINALIZADO, ShipmentStatus.FINALIZADO);
        emCurso = transport(ShipmentStatus.FINALIZADO, ShipmentStatus.AGUARDANDO_INICIO);
        futura = transport(ShipmentStatus.AGUARDANDO_INICIO, ShipmentStatus.AGUARDANDO_INICIO);
        when(routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(any()))
                .thenAnswer(inv -> stopsByTransport.getOrDefault(inv.getArgument(0), List.of()));
        vehicleTrips(futura, emCurso, concluida); // mais recente primeiro
    }

    private Transport transport(ShipmentStatus... statuses) {
        Transport t = new Transport();
        t.setId(UUID.randomUUID());
        stopsByTransport.put(t.getId(), Arrays.stream(statuses).map(st -> {
            Shipment s = new Shipment();
            s.setId(UUID.randomUUID());
            s.setStatus(st);
            RouteStop rs = new RouteStop();
            rs.setShipment(s);
            rs.setTransport(t);
            return rs;
        }).toList());
        return t;
    }

    private void vehicleTrips(Transport... newestFirst) {
        when(transportRepository.findByEquipament1IdOrderByCreatedAtDesc(veiculo.getId())).thenReturn(List.of(newestFirst));
    }

    @Test
    @DisplayName("Sem viagem informada: usa a viagem em execução, não a mais recente")
    void semVinculoUsaViagemEmExecucao() {
        assertEquals(emCurso, resolver.resolve(veiculo, null, false).transport());
        assertEquals(emCurso, resolver.resolve(veiculo, null, true).transport());
    }

    @Test
    @DisplayName("Viagem informada pelo app vale mesmo antes de o backend saber que ela começou")
    void vinculoValeMesmoSemInicioRegistrado() {
        ActiveTripResolver.ResolvedTrip trip = resolver.resolve(veiculo, futura, false);
        assertNotNull(trip);
        assertEquals(futura, trip.transport());
        verifyNoInteractions(transportRepository);
    }

    @Test
    @DisplayName("Viagem informada já concluída: ponto não vai para nenhuma viagem")
    void vinculoComViagemConcluidaDescarta() {
        assertNull(resolver.resolve(veiculo, concluida, false));
        assertNull(resolver.resolve(veiculo, concluida, true));
    }

    @Test
    @DisplayName("Nenhuma em execução: rota descarta; geofencing usa a aberta mais recente para iniciar a viagem")
    void nenhumaEmExecucao() {
        vehicleTrips(futura, concluida);
        assertNull(resolver.resolve(veiculo, null, false));
        assertEquals(futura, resolver.resolve(veiculo, null, true).transport());
    }

    @Test
    @DisplayName("Todas concluídas: ponto não pertence a nenhuma viagem")
    void todasConcluidas() {
        vehicleTrips(concluida);
        assertNull(resolver.resolve(veiculo, null, true));
    }
}
