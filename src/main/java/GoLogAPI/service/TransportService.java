package GoLogAPI.service;

import GoLogAPI.dto.transport.TransportCreateRequest;
import GoLogAPI.dto.transport.TransportCreateResponse;
import GoLogAPI.dto.transport.TransportResponse;
import GoLogAPI.dto.transport.TransportUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TransportMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.Transport;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.TransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportMapper transportMapper;
    private final DriverRepository driverRepository;
    private final CompanyRepository companyRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;

    public TransportService(
            TransportRepository transportRepository, TransportMapper transportMapper,
            DriverRepository driverRepository, CompanyRepository companyRepository,
            EquipamentGroupRepository equipamentGroupRepository){
        this.transportRepository = transportRepository;
        this.transportMapper = transportMapper;
        this.driverRepository = driverRepository;
        this.companyRepository = companyRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    @Transactional
    public TransportCreateResponse save(TransportCreateRequest transportCreateRequest){
        Transport transport = transportMapper.toEntity(transportCreateRequest);
        Driver driver = driverRepository.findById(transportCreateRequest.driverId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.driverId()));
        Company company = companyRepository.findById(transportCreateRequest.transporterId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.transporterId()));
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(transportCreateRequest.equipamentGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.equipamentGroupId()));
        transport.setDriver(driver);
        transport.setTransporter(company);
        transport.setEquipamentGroup(equipamentGroup);
        transportRepository.save(transport);
        return transportMapper.toCreateResponse(transport);
    }

    public TransportResponse get(UUID id){
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return transportMapper.toResponse(transport);
    }

    @Transactional
    public void delete(UUID id){
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        transportRepository.delete(transport);
    }

    @Transactional
    public TransportResponse update(UUID id, TransportCreateRequest transportCreateRequest){
        transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Transport transport = transportMapper.toEntity(transportCreateRequest);

        Driver driver = driverRepository.findById(transportCreateRequest.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.driverId()));

        Company company = companyRepository.findById(transportCreateRequest.transporterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.transporterId()));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(transportCreateRequest.equipamentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageException.NOT_FOUND_MESSAGE, transportCreateRequest.equipamentGroupId()));

        transport.setDriver(driver);
        transport.setEquipamentGroup(equipamentGroup);
        transport.setTransporter(company);
        transport.setId(id);
        transportRepository.save(transport);
        return transportMapper.toResponse(transport);
    }

    @Transactional
    public TransportResponse updatePartial(UUID id, TransportUpdateRequest transportUpdateRequest){
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(transportUpdateRequest.routeReturnPlanned() != null && !transportUpdateRequest.routeReturnPlanned().isBlank())
            transport.setRouteReturnPlanned(transportUpdateRequest.routeReturnPlanned());
        if(transportUpdateRequest.routeReturnCompleted() != null && !transportUpdateRequest.routeReturnCompleted().isBlank())
            transport.setRouteReturnCompleted(transportUpdateRequest.routeReturnCompleted());
        if(transportUpdateRequest.deliveryQuantity() != null)
            transport.setDeliveryQuantity(transportUpdateRequest.deliveryQuantity());
        if(transportUpdateRequest.totalKilometer() != null)
            transport.setTotalKilometer(transportUpdateRequest.totalKilometer());
        if(transportUpdateRequest.timeStopped() != null)
            transport.setTimeStopped(transportUpdateRequest.timeStopped());
        if(transportUpdateRequest.totalTime() != null)
            transport.setTotalTime(transportUpdateRequest.totalTime());
        if(transportUpdateRequest.driverId() != null){
            Driver driver = driverRepository.findById(transportUpdateRequest.driverId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            MessageException.NOT_FOUND_MESSAGE, transportUpdateRequest.driverId()));
            transport.setDriver(driver);
        }
        if(transportUpdateRequest.transporterId() != null){
            Company company = companyRepository.findById(transportUpdateRequest.transporterId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            MessageException.NOT_FOUND_MESSAGE, transportUpdateRequest.transporterId()));
            transport.setTransporter(company);
        }
        if(transportUpdateRequest.equipamentGroupId() != null){
            EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(transportUpdateRequest.equipamentGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            MessageException.NOT_FOUND_MESSAGE, transportUpdateRequest.equipamentGroupId()));
            transport.setEquipamentGroup(equipamentGroup);
        }

        transportRepository.save(transport);
        return transportMapper.toResponse(transport);
    }
}
