package GoLogAPI.controller;

import GoLogAPI.dto.shipmentDemand.ShipmentDemandRequest;
import GoLogAPI.dto.shipmentDemand.ShipmentDemandResponse;
import GoLogAPI.service.ShipmentDemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shipment-demand")
@Tag(name = "Demandas da Carga", description = "Associação de quantidades de demandas dinâmicas por carga")
public class ShipmentDemandController {

    private final ShipmentDemandService shipmentDemandService;

    public ShipmentDemandController(ShipmentDemandService shipmentDemandService) {
        this.shipmentDemandService = shipmentDemandService;
    }

    @Operation(summary = "Definir Demandas da Carga", description = "Associa ou atualiza a lista de demandas dinâmicas de uma carga")
    @PostMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<ShipmentDemandResponse>> saveDemands(
            @PathVariable("shipmentId") UUID shipmentId,
            @Valid @RequestBody List<ShipmentDemandRequest> requests) {
        return ResponseEntity.ok(shipmentDemandService.saveDemands(shipmentId, requests));
    }

    @Operation(summary = "Listar Demandas da Carga", description = "Retorna todas as demandas associadas à carga")
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<ShipmentDemandResponse>> getDemandsByShipmentId(@PathVariable("shipmentId") UUID shipmentId) {
        return ResponseEntity.ok(shipmentDemandService.getDemandsByShipmentId(shipmentId));
    }
}
