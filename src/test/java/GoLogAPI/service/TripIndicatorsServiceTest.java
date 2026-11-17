package GoLogAPI.service;

import GoLogAPI.dto.transport.TripIndicatorsResponse.ScheduleAdherence;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Tractor;
import GoLogAPI.model.enums.TypeFuel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripIndicatorsServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 12, 0);

    private static RouteStop stop(LocalDateTime scheduled, LocalDateTime arrived) {
        Shipment s = new Shipment();
        s.setSchedulind(scheduled);
        RouteStop rs = new RouteStop();
        rs.setShipment(s);
        rs.setArrivedAt(arrived);
        return rs;
    }

    @Test
    void agendaAtendidaComparaChegadaComHorarioAgendado() {
        ScheduleAdherence a = TripIndicatorsService.schedule(List.of(
                stop(NOW.minusHours(2), NOW.minusHours(3)),   // no horário
                stop(NOW.minusHours(2), NOW.minusHours(1)),   // atrasada
                stop(NOW.minusHours(1), null),                // vencida sem chegada
                stop(NOW.plusHours(1), null),                 // ainda no prazo
                stop(null, NOW)                               // sem agenda: fora da conta
        ), NOW);
        assertEquals(4, a.scheduled());
        assertEquals(1, a.metOnTime());
        assertEquals(2, a.late());
        assertEquals(1, a.pending());
        assertEquals(33.3, a.onTimeRate());
    }

    @Test
    void combustivelUsaConsumoMedioENaoPresumeFator() {
        Tractor diesel = new Tractor();
        diesel.setTypeFuel(TypeFuel.DIESEL);
        diesel.setKmPerLiter(2.5);
        assertEquals(40.0, TripIndicatorsService.fuelLiters(diesel, 100), 0.001);

        Tractor semConsumo = new Tractor();
        semConsumo.setTypeFuel(TypeFuel.DIESEL);
        assertNull(TripIndicatorsService.fuelLiters(semConsumo, 100));

        Tractor eletrico = new Tractor();
        eletrico.setTypeFuel(TypeFuel.ELETRICO);
        assertEquals(0.0, TripIndicatorsService.fuelLiters(eletrico, 100));
    }
}
