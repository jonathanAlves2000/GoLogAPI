package GoLogAPI.service.routeOptimization;

import com.google.protobuf.Duration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coleta com 3 entregas filhas vai ao otimizador como 3 visitas de coleta no mesmo endereço. O tempo de parada
 * (15 min no perfil) deve entrar uma vez só no tempo de operação e no custo de horas (caso da viagem 29).
 */
class PickupDurationTest {

    private static final Duration QUINZE_MIN = Duration.newBuilder().setSeconds(900).build();

    @Test
    void coletaComTresFilhasCobraAParadaUmaVez() {
        Set<UUID> cobradas = new HashSet<>();
        UUID coleta = UUID.randomUUID();
        long total = 0;
        for (int filha = 0; filha < 3; filha++) {
            total += RouteRequestService.pickupDuration(cobradas, coleta, QUINZE_MIN).getSeconds();
        }
        assertEquals(900, total);
    }

    @Test
    void coletasDiferentesCobramCadaUma() {
        Set<UUID> cobradas = new HashSet<>();
        assertEquals(900, RouteRequestService.pickupDuration(cobradas, UUID.randomUUID(), QUINZE_MIN).getSeconds());
        assertEquals(900, RouteRequestService.pickupDuration(cobradas, UUID.randomUUID(), QUINZE_MIN).getSeconds());
    }

    @Test
    void viagem29TempoDeParadasEsperado() {
        // 1 coleta (3 filhas) + 3 entregas, 15 min cada: 60 min de paradas, não 90
        Set<UUID> cobradas = new HashSet<>();
        UUID coleta = UUID.randomUUID();
        long paradas = 0;
        for (int filha = 0; filha < 3; filha++) {
            paradas += RouteRequestService.pickupDuration(cobradas, coleta, QUINZE_MIN).getSeconds(); // visita de coleta
            paradas += QUINZE_MIN.getSeconds();                                                      // visita de entrega
        }
        assertEquals(3600, paradas);
    }
}
