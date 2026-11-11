package GoLogAPI.service;

import GoLogAPI.dto.shipmentType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.shipmentType.DeliveryTypeResponse;
import GoLogAPI.dto.shipmentType.DeliveryTypeUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.model.Company;
import GoLogAPI.model.ShipmentType;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.ShipmentTypeRepository;
import GoLogAPI.validation.DeliveryTypeValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShipmentTypeService {

    private final ShipmentTypeRepository shipmentTypeRepository;
    private final DeliveryTypeValidate deliveryTypeValidate;
    private final CompanyRepository companyRepository;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public ShipmentTypeService(ShipmentTypeRepository shipmentTypeRepository,
                               DeliveryTypeValidate deliveryTypeValidate,
                               CompanyRepository companyRepository,
                               GoLogAPI.infra.tenant.TenantGuard tenantGuard){
        this.tenantGuard = tenantGuard;
        this.deliveryTypeValidate = deliveryTypeValidate;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public DeliveryTypeResponse save(DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        deliveryTypeValidate.validate(deliveryTypeCreateRequest);

        Company company = tenantGuard.ownerForCatalog(null);

        ShipmentType shipmentType = ShipmentType.builder()
                .name(deliveryTypeCreateRequest.name())
                .description(deliveryTypeCreateRequest.description())
                .care(deliveryTypeCreateRequest.care())
                .company(company)
                .build();

        shipmentType = shipmentTypeRepository.save(shipmentType);

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

    public DeliveryTypeResponse get(UUID id){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogRead(shipmentType.getCompany(), id);

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

    public List<DeliveryTypeResponse> getAll() {
        UUID currentTenant = TenantContext.getCurrentTenantId();
        List<ShipmentType> shipmentTypes = shipmentTypeRepository.findAvailableForCompany(currentTenant);
        return shipmentTypes.stream()
                .map(st -> new DeliveryTypeResponse(st.getId(), st.getName(), st.getDescription(), st.getCare()))
                .toList();
    }

    @Transactional
    public void delete(UUID id){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(shipmentType.getCompany(), id);
        shipmentTypeRepository.delete(shipmentType);
    }

    @Transactional
    public DeliveryTypeResponse update(UUID id, DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(shipmentType.getCompany(), id);

        deliveryTypeValidate.validate(deliveryTypeCreateRequest);

       shipmentType.setName(deliveryTypeCreateRequest.name());
       shipmentType.setDescription(deliveryTypeCreateRequest.description());
       shipmentType.setCare(deliveryTypeCreateRequest.care());

       shipmentTypeRepository.save(shipmentType);

       return new DeliveryTypeResponse(
               shipmentType.getId(),
               shipmentType.getName(),
               shipmentType.getDescription(),
               shipmentType.getCare()
       );
    }

    @Transactional
    public DeliveryTypeResponse updatePartial(UUID id, DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkCatalogWrite(shipmentType.getCompany(), id);

        deliveryTypeValidate.validate(deliveryTypeUpdateRequest);

        if(deliveryTypeUpdateRequest.name() != null && !deliveryTypeUpdateRequest.name().isBlank())
            shipmentType.setName(deliveryTypeUpdateRequest.name());
        if(deliveryTypeUpdateRequest.description() != null && !deliveryTypeUpdateRequest.description().isBlank())
            shipmentType.setDescription(deliveryTypeUpdateRequest.description());
        if(deliveryTypeUpdateRequest.care() != null && !deliveryTypeUpdateRequest.care().isBlank())
            shipmentType.setCare(deliveryTypeUpdateRequest.care());

        shipmentTypeRepository.save(shipmentType);

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

}
