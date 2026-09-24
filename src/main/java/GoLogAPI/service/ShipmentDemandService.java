package GoLogAPI.service;

import GoLogAPI.dto.shipmentDemand.ShipmentDemandRequest;
import GoLogAPI.dto.shipmentDemand.ShipmentDemandResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.DemandType;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.ShipmentDemand;
import GoLogAPI.repository.DemandTypeRepository;
import GoLogAPI.repository.ShipmentDemandRepository;
import GoLogAPI.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShipmentDemandService {

    private final ShipmentDemandRepository shipmentDemandRepository;
    private final ShipmentRepository shipmentRepository;
    private final DemandTypeRepository demandTypeRepository;

    public ShipmentDemandService(ShipmentDemandRepository shipmentDemandRepository,
                                 ShipmentRepository shipmentRepository,
                                 DemandTypeRepository demandTypeRepository) {
        this.shipmentDemandRepository = shipmentDemandRepository;
        this.shipmentRepository = shipmentRepository;
        this.demandTypeRepository = demandTypeRepository;
    }

    @Transactional
    public List<ShipmentDemandResponse> saveDemands(UUID shipmentId, List<ShipmentDemandRequest> requests) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentId));

        // Substitui demandas anteriores
        shipmentDemandRepository.deleteByShipmentId(shipmentId);

        List<ShipmentDemand> demandsToSave = new ArrayList<>();
        for (ShipmentDemandRequest req : requests) {
            DemandType demandType = demandTypeRepository.findById(req.demandTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de demanda não encontrado: ", req.demandTypeId()));

            ShipmentDemand demand = ShipmentDemand.builder()
                    .shipment(shipment)
                    .demandType(demandType)
                    .amount(req.amount())
                    .build();
            demandsToSave.add(demand);
        }

        List<ShipmentDemand> saved = shipmentDemandRepository.saveAll(demandsToSave);
        return saved.stream().map(this::toResponse).toList();
    }

    public List<ShipmentDemandResponse> getDemandsByShipmentId(UUID shipmentId) {
        return shipmentDemandRepository.findByShipmentId(shipmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ShipmentDemandResponse toResponse(ShipmentDemand entity) {
        return new ShipmentDemandResponse(
                entity.getId(),
                entity.getDemandType().getId(),
                entity.getDemandType().getCode(),
                entity.getDemandType().getName(),
                entity.getDemandType().getUnit(),
                entity.getAmount()
        );
    }
}
