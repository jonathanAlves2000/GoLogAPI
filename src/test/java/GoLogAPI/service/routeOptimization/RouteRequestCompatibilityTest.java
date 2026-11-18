package GoLogAPI.service.routeOptimization;

import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.TypeTransport;
import com.google.maps.routeoptimization.v1.ShipmentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RouteRequestCompatibilityTest {

    @Test
    @DisplayName("Moto com baú climatizado leva a carga climatizada e moto sem baú não pode levar")
    void shouldAssignClimatizedShipmentOnlyToEquippedVehicle() {
        TypeTransport climatizada = new TypeTransport();
        climatizada.setId(UUID.randomUUID());
        climatizada.setName("Climatizada");
        climatizada.setRequiresCompatibleVehicle(true);

        // Veículo 0: Moto equipada para Climatizada
        EquipamentGroup motoClimatizada = new EquipamentGroup();
        motoClimatizada.setId(UUID.randomUUID());
        motoClimatizada.setTypeTransports(Set.of(climatizada));

        // Veículo 1: Moto comum (sem baú climatizado)
        EquipamentGroup motoComum = new EquipamentGroup();
        motoComum.setId(UUID.randomUUID());
        motoComum.setTypeTransports(Set.of());

        List<EquipamentGroup> vehicleGroups = List.of(motoClimatizada, motoComum);
        List<RouteRequestService.PreSkipped> preSkipped = new ArrayList<>();
        ShipmentModel.Builder modelBuilder = ShipmentModel.newBuilder();

        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTypeTransport(climatizada);

        com.google.maps.routeoptimization.v1.Shipment.Builder proto =
                com.google.maps.routeoptimization.v1.Shipment.newBuilder().setLabel("delivery-climatizada");

        RouteRequestService.addWithCompatibility(modelBuilder, proto, List.of(shipment), vehicleGroups, preSkipped);

        assertTrue(preSkipped.isEmpty(), "Não deve ser preSkipped pois há veículo compatível");
        assertEquals(1, modelBuilder.getShipmentsCount(), "Remessa deve ser adicionada ao modelo");

        com.google.maps.routeoptimization.v1.Shipment addedShipment = modelBuilder.getShipments(0);
        assertEquals(List.of(0), addedShipment.getAllowedVehicleIndicesList(),
                "Apenas o veículo índice 0 (moto com baú climatizado) deve estar habilitado");
    }

    @Test
    @DisplayName("Sem veículo compatível escalado: remessa é rejeitada antes do Google com motivo explicativo")
    void shouldPreSkipShipmentWhenNoVehicleIsEquipped() {
        TypeTransport refrigerada = new TypeTransport();
        refrigerada.setId(UUID.randomUUID());
        refrigerada.setName("Refrigerada");
        refrigerada.setRequiresCompatibleVehicle(true);

        TypeTransport climatizada = new TypeTransport();
        climatizada.setId(UUID.randomUUID());
        climatizada.setName("Climatizada");
        climatizada.setRequiresCompatibleVehicle(true);

        // Apenas moto comum e moto climatizada escaladas — nenhuma tem baú refrigerado
        EquipamentGroup motoClimatizada = new EquipamentGroup();
        motoClimatizada.setId(UUID.randomUUID());
        motoClimatizada.setTypeTransports(Set.of(climatizada));

        EquipamentGroup motoComum = new EquipamentGroup();
        motoComum.setId(UUID.randomUUID());
        motoComum.setTypeTransports(Set.of());

        List<EquipamentGroup> vehicleGroups = List.of(motoClimatizada, motoComum);
        List<RouteRequestService.PreSkipped> preSkipped = new ArrayList<>();
        ShipmentModel.Builder modelBuilder = ShipmentModel.newBuilder();

        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTypeTransport(refrigerada);

        com.google.maps.routeoptimization.v1.Shipment.Builder proto =
                com.google.maps.routeoptimization.v1.Shipment.newBuilder().setLabel("delivery-refrigerada");

        RouteRequestService.addWithCompatibility(modelBuilder, proto, List.of(shipment), vehicleGroups, preSkipped);

        assertEquals(0, modelBuilder.getShipmentsCount(), "Remessa NÃO deve ser enviada ao Google");
        assertEquals(1, preSkipped.size(), "Remessa deve estar em preSkipped");

        RouteRequestService.PreSkipped skipped = preSkipped.get(0);
        assertTrue(skipped.reasons().get(0).contains("nenhum veículo escalado está equipado para Refrigerada"),
                "Motivo deve explicar claramente a ausência de equipamento");
    }

    @Test
    @DisplayName("Carga comum sem exigência especial pode ser atendida por qualquer veículo")
    void shouldAllowAnyVehicleForStandardShipment() {
        TypeTransport cargaSeca = new TypeTransport();
        cargaSeca.setId(UUID.randomUUID());
        cargaSeca.setName("Carga Seca");
        cargaSeca.setRequiresCompatibleVehicle(false); // Não exige veículo compatível

        EquipamentGroup motoClimatizada = new EquipamentGroup();
        motoClimatizada.setId(UUID.randomUUID());
        motoClimatizada.setTypeTransports(Set.of());

        EquipamentGroup motoComum = new EquipamentGroup();
        motoComum.setId(UUID.randomUUID());
        motoComum.setTypeTransports(Set.of());

        List<EquipamentGroup> vehicleGroups = List.of(motoClimatizada, motoComum);
        List<RouteRequestService.PreSkipped> preSkipped = new ArrayList<>();
        ShipmentModel.Builder modelBuilder = ShipmentModel.newBuilder();

        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTypeTransport(cargaSeca);

        com.google.maps.routeoptimization.v1.Shipment.Builder proto =
                com.google.maps.routeoptimization.v1.Shipment.newBuilder().setLabel("delivery-padrao");

        RouteRequestService.addWithCompatibility(modelBuilder, proto, List.of(shipment), vehicleGroups, preSkipped);

        assertTrue(preSkipped.isEmpty());
        assertEquals(1, modelBuilder.getShipmentsCount());
        assertTrue(modelBuilder.getShipments(0).getAllowedVehicleIndicesList().isEmpty(),
                "Sem allowedVehicleIndices significa que qualquer veículo escalado pode atender");
    }
}
