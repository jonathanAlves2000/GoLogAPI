package GoLogAPI.service.telemetry;

import GoLogAPI.dto.telemetry.GeofenceEventResponse;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RouteStopStatus;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TransportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeofencingServiceTest {

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private GeofencingService geofencingService;

    @Test
    @DisplayName("Deve calcular distância Haversine com alta precisão e detectar proximidade")
    void shouldCalculateHaversineDistanceAccurately() {
        // Ponto 1: Marco Zero da Praça da Sé, SP (-23.550520, -46.633308)
        double lat1 = -23.550520;
        double lon1 = -46.633308;

        // Ponto 2: Próximo à Catedral da Sé (~80 metros de distância) (-23.551200, -46.633800)
        double lat2 = -23.551200;
        double lon2 = -46.633800;

        double distance = GeofencingService.calculateHaversineDistanceMeters(lat1, lon1, lat2, lon2);

        assertTrue(distance > 0, "Distância deve ser positiva");
        assertTrue(distance < 150, "Distância deve ser menor que 150 metros");
        assertTrue(distance <= GeofencingService.DEFAULT_GEOFENCE_RADIUS_METERS, "Deve estar dentro do raio padrão de geofence de 200m");
    }

    @Test
    @DisplayName("Deve normalizar placas de veículos corretamente eliminando caracteres especiais")
    void shouldNormalizeVehiclePlates() {
        assertEquals("ABC1234", VehiclePlateCacheService.normalizePlate("ABC-1234"));
        assertEquals("BRA2E19", VehiclePlateCacheService.normalizePlate("bra2e19"));
        assertEquals("XYZ9999", VehiclePlateCacheService.normalizePlate(" XYZ - 9999 "));
    }

    @Test
    @DisplayName("Deve disparar check-in automático quando veículo entra no raio da parada")
    void shouldTriggerAutoCheckInWhenVehicleEntersGeofence() {
        UUID equipamentId = UUID.randomUUID();
        Equipament equipament = new Equipament();
        equipament.setId(equipamentId);
        equipament.setPlate("ABC1D23");

        UUID transportId = UUID.randomUUID();
        Transport transport = new Transport();
        transport.setId(transportId);

        Address address = new Address();
        address.setLatitude("-23.550520");
        address.setLongitude("-46.633308");

        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setAddress(address);
        shipment.setStatus(ShipmentStatus.AGUARDANDO_INICIO);

        RouteStop routeStop = new RouteStop();
        routeStop.setId(UUID.randomUUID());
        routeStop.setSequenceOrder(1);
        routeStop.setShipment(shipment);
        routeStop.setTransport(transport);
        routeStop.setStatus(RouteStopStatus.PENDENTE);

        when(transportRepository.findByEquipament1IdOrderByCreatedAtDesc(equipamentId))
                .thenReturn(List.of(transport));

        when(routeStopRepository.findByTransportIdAndArrivedAtIsNullOrderBySequenceOrderAsc(transportId))
                .thenReturn(List.of(routeStop));

        // Coordenada do caminhão a ~50 metros do ponto de entrega
        double vehicleLat = -23.550800;
        double vehicleLon = -46.633500;
        LocalDateTime now = LocalDateTime.now();

        Optional<GeofenceEventResponse> eventOpt = geofencingService.checkGeofenceArrival(
                equipament, vehicleLat, vehicleLon, now, 15.0
        );

        assertTrue(eventOpt.isPresent(), "Check-in automático deve ser disparado");
        GeofenceEventResponse event = eventOpt.get();

        assertEquals("AUTO_CHECK_IN", event.eventType());
        assertEquals("ABC1D23", event.plate());
        assertEquals(1, event.sequenceOrder());
        assertEquals(RouteStopStatus.CHEGOU, event.status());

        // Verifica se a parada e o shipment foram atualizados e persistidos
        assertEquals(RouteStopStatus.CHEGOU, routeStop.getStatus());
        assertTrue(routeStop.getAutoCheckIn());
        assertNotNull(routeStop.getArrivedAt());
        assertEquals(ShipmentStatus.INICIADO, shipment.getStatus());

        verify(routeStopRepository, times(1)).save(routeStop);
        verify(shipmentRepository, times(1)).save(shipment);
    }
}
