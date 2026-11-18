package GoLogAPI.service;

import GoLogAPI.dto.transport.TransportCreateRequest;
import GoLogAPI.dto.transport.TransportCreateResponse;
import GoLogAPI.dto.transport.TransportResponse;
import GoLogAPI.dto.transport.TransportUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.TransportMapper;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RouteStopStatus;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.service.telemetry.GeofencingService;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransportService {

    private final TransportRepository transportRepository;
    private final TransportMapper transportMapper;
    private final DriverRepository driverRepository;
    private final CompanyRepository companyRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;
    private final GoLogAPI.repository.RouteStopRepository routeStopRepository;
    private final GoLogAPI.repository.ShipmentRepository shipmentRepository;

    private GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher;

    @org.springframework.beans.factory.annotation.Autowired
    void setShipmentEventPublisher(GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher) {
        this.shipmentEventPublisher = shipmentEventPublisher;
    }

    public TransportService(
            TransportRepository transportRepository, TransportMapper transportMapper,
            DriverRepository driverRepository, CompanyRepository companyRepository,
            EquipamentGroupRepository equipamentGroupRepository,
            GoLogAPI.infra.tenant.TenantGuard tenantGuard,
            GoLogAPI.repository.RouteStopRepository routeStopRepository,
            GoLogAPI.repository.ShipmentRepository shipmentRepository){
        this.tenantGuard = tenantGuard;
        this.routeStopRepository = routeStopRepository;
        this.shipmentRepository = shipmentRepository;
        this.transportRepository = transportRepository;
        this.transportMapper = transportMapper;
        this.driverRepository = driverRepository;
        this.companyRepository = companyRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    @Transactional
    public TransportCreateResponse save(TransportCreateRequest transportCreateRequest){

        Transport transport = transportMapper.toEntity(transportCreateRequest);

        Driver driver = findDriver(transportCreateRequest.driverId());
        Company company = tenantGuard.ownerForWrite(transportCreateRequest.transporterId());
        EquipamentGroup equipamentGroup = findGroup(transportCreateRequest.equipamentGroupId());
        if (transport.getRoutePlanned() == null) transport.setRoutePlanned("");
        if (transport.getShipmentQuantity() == null) transport.setShipmentQuantity(0);
        if (transport.getCalculedDistance() == null) transport.setCalculedDistance(0);
        if (transport.getTimeStoppedCalculed() == null) transport.setTimeStoppedCalculed(0);
        if (transport.getTravelDuration() == null) transport.setTravelDuration(0);
        if (transport.getTotalCostCalculed() == null) transport.setTotalCostCalculed(0.0);
        if (transport.getTotalTimeCalculed() == null) transport.setTotalTimeCalculed(0);
        if (transport.getCostKmCalculed() == null) transport.setCostKmCalculed(0.0);
        if (transport.getCostHourCalculed() == null) transport.setCostHourCalculed(0.0);

        transport.setDriver(driver);
        transport.setTransporter(company);
        transport.setEquipamentGroup(equipamentGroup);
        transportRepository.save(transport);

        if (transportCreateRequest.shipmentIds() != null && !transportCreateRequest.shipmentIds().isEmpty()) {
            List<LatLng> routePoints = new ArrayList<>();
            Address originAddr = (equipamentGroup.getCompany() != null && equipamentGroup.getCompany().getAddress() != null)
                    ? equipamentGroup.getCompany().getAddress()
                    : company.getAddress();
            if (originAddr != null && originAddr.getLatitude() != null && originAddr.getLongitude() != null) {
                try {
                    routePoints.add(new LatLng(
                            Double.parseDouble(originAddr.getLatitude()),
                            Double.parseDouble(originAddr.getLongitude())
                    ));
                } catch (Exception ignored) {}
            }

            int sequence = 1;
            int totalDistance = 0;
            LatLng lastPoint = routePoints.isEmpty() ? null : routePoints.get(0);

            for (UUID shipmentId : transportCreateRequest.shipmentIds()) {
                Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
                if (shipment == null) continue;
                tenantGuard.checkAccess(shipment.getCompany(), shipmentId);

                RouteStop stop = new RouteStop();
                stop.setTransport(transport);
                stop.setShipment(shipment);
                stop.setSequenceOrder(sequence++);
                stop.setStatus(RouteStopStatus.PENDENTE);
                stop.setWeight(shipment.getWeight());
                stop.setVolume(shipment.getVolume());

                Address addr = shipment.getAddress() != null ? shipment.getAddress()
                        : (shipment.getCustomer() != null ? shipment.getCustomer().getAddress() : null);
                if (addr != null && addr.getLatitude() != null && addr.getLongitude() != null) {
                    try {
                        LatLng stopPt = new LatLng(
                                Double.parseDouble(addr.getLatitude()),
                                Double.parseDouble(addr.getLongitude())
                        );
                        routePoints.add(stopPt);
                        if (lastPoint != null) {
                            totalDistance += (int) Math.round(GeofencingService.calculateHaversineDistanceMeters(
                                    lastPoint.lat, lastPoint.lng, stopPt.lat, stopPt.lng
                            ));
                        }
                        lastPoint = stopPt;
                    } catch (Exception ignored) {}
                }

                routeStopRepository.save(stop);
                shipment.setStatus(ShipmentStatus.AGUARDANDO_INICIO);
                shipmentRepository.save(shipment);
                if (shipmentEventPublisher != null) {
                    shipmentEventPublisher.publish("SHIPMENT_PLANNED", shipment);
                }
            }

            transport.setShipmentQuantity(sequence - 1);
            if (!routePoints.isEmpty() && (transport.getRoutePlanned() == null || transport.getRoutePlanned().isBlank())) {
                transport.setRoutePlanned(PolylineEncoding.encode(routePoints));
            }
            if (transport.getCalculedDistance() == null || transport.getCalculedDistance() == 0) {
                transport.setCalculedDistance(totalDistance);
            }
            transportRepository.save(transport);
        }

        return transportMapper.toCreateResponse(transport);
    }

    public TransportResponse get(UUID id){
        Transport transport = findVisible(id);
        return transportMapper.toResponse(transport);
    }

    public List<TransportResponse> getAll(){
        List<Transport> transports = tenantGuard.isGlobalView()
                ? transportRepository.findAll()
                : transportRepository.findByTransporterId(tenantGuard.scope());
        return transportMapper.toResponses(transports);
    }

    @Transactional
    public void delete(UUID id){
        Transport transport = findVisible(id);
        // Devolve as remessas planejadas para a fila de pendentes antes de cancelar o transporte
        for (GoLogAPI.model.RouteStop stop : routeStopRepository.findByTransportId(id)) {
            GoLogAPI.model.Shipment shipment = stop.getShipment();
            if (shipment != null && shipment.getStatus() == GoLogAPI.model.enums.ShipmentStatus.AGUARDANDO_INICIO) {
                shipment.setStatus(GoLogAPI.model.enums.ShipmentStatus.PENDENTE);
                shipmentRepository.save(shipment);
                shipmentEventPublisher.publish("SHIPMENT_UNPLANNED", shipment);
            }
        }
        routeStopRepository.deleteByTransportId(id);
        transportRepository.delete(transport);
    }

    @Transactional
    public TransportResponse update(UUID id, TransportCreateRequest request){
        Transport transport = findVisible(id);
        if (request.routeCompleted() != null) transport.setRouteCompleted(request.routeCompleted());
        if (request.deliveryQuantity() != null) transport.setShipmentQuantity(request.deliveryQuantity());
        if (request.distanceTraveled() != null) transport.setDistanceTraveled(request.distanceTraveled());
        if (request.timeStopped() != null) transport.setTimeStopped(request.timeStopped().intValue());
        if (request.totalTime() != null) transport.setTotalTime(request.totalTime().intValue());
        transport.setDriver(findDriver(request.driverId()));
        transport.setEquipamentGroup(findGroup(request.equipamentGroupId()));
        transport.setTransporter(tenantGuard.ownerForWrite(request.transporterId()));
        transportRepository.save(transport);
        return transportMapper.toResponse(transport);
    }

    @Transactional
    public TransportResponse updatePartial(UUID id, TransportUpdateRequest transportUpdateRequest){
        Transport transport = findVisible(id);

        if(transportUpdateRequest.routeReturnPlanned() != null && !transportUpdateRequest.routeReturnPlanned().isBlank())
            transport.setRouteReturnPlanned(transportUpdateRequest.routeReturnPlanned());
        if(transportUpdateRequest.routeReturnCompleted() != null && !transportUpdateRequest.routeReturnCompleted().isBlank())
            transport.setRouteReturnCompleted(transportUpdateRequest.routeReturnCompleted());
        if(transportUpdateRequest.deliveryQuantity() != null)
            transport.setShipmentQuantity(transportUpdateRequest.deliveryQuantity());
        if(transportUpdateRequest.totalKilometer() != null)
            transport.setDistanceTraveled(transportUpdateRequest.totalKilometer());
        if(transportUpdateRequest.timeStopped() != null)
            transport.setTimeStopped(transportUpdateRequest.timeStopped());
        if(transportUpdateRequest.totalTime() != null)
            transport.setTotalTime(transportUpdateRequest.totalTime());
        if(transportUpdateRequest.driverId() != null){
            Driver driver = findDriver(transportUpdateRequest.driverId());
            transport.setDriver(driver);
        }
        if(transportUpdateRequest.transporterId() != null){
            Company company = tenantGuard.ownerForWrite(transportUpdateRequest.transporterId());
            transport.setTransporter(company);
        }
        if(transportUpdateRequest.equipamentGroupId() != null){
            EquipamentGroup equipamentGroup = findGroup(transportUpdateRequest.equipamentGroupId());
            transport.setEquipamentGroup(equipamentGroup);
        }

        transportRepository.save(transport);
        return transportMapper.toResponse(transport);
    }

    public Transport findVisible(UUID id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: ", id));
        tenantGuard.checkAccess(transport.getTransporter(), id);
        return transport;
    }

    private Driver findDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: ", id));
        tenantGuard.checkAccess(driver.getCompany(), id);
        return driver;
    }

    private EquipamentGroup findGroup(UUID id) {
        EquipamentGroup group = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conjunto não encontrado: ", id));
        tenantGuard.checkAccess(group.getCompany(), id);
        return group;
    }
}
