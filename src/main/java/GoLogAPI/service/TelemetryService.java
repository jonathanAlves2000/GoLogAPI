package GoLogAPI.service;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryResponseList;
import GoLogAPI.dto.telemetry.TelemetryUpdateRequest;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TelemetryMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.Telemetry;
import GoLogAPI.repository.EquipamentRepository;
import GoLogAPI.repository.TelemetryRepository;
import GoLogAPI.service.telemetry.GeofencingService;
import GoLogAPI.service.telemetry.TelemetryTripLinkResolver;
import GoLogAPI.service.telemetry.TelemetryTripLinkResolver.TripLink;
import GoLogAPI.service.telemetry.TripTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryMapper telemetryMapper;
    private final EquipamentRepository equipamentRepository;

    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;
    private final TelemetryTripLinkResolver tripLinkResolver;
    private final TripTrackingService tripTrackingService;
    private final GeofencingService geofencingService;

    public TelemetryService(TelemetryRepository telemetryRepository,
                            TelemetryMapper telemetryMapper, EquipamentRepository equipamentRepository,
                            GoLogAPI.infra.tenant.TenantGuard tenantGuard,
                            TelemetryTripLinkResolver tripLinkResolver,
                            TripTrackingService tripTrackingService,
                            GeofencingService geofencingService){
        this.tenantGuard = tenantGuard;
        this.tripLinkResolver = tripLinkResolver;
        this.tripTrackingService = tripTrackingService;
        this.geofencingService = geofencingService;
        this.telemetryRepository = telemetryRepository;
        this.telemetryMapper = telemetryMapper;
        this.equipamentRepository = equipamentRepository;
    }

    @Transactional
    public TelemetryReponse save(TelemetryCreateRequest telemetryCreateRequest){
       Telemetry telemetry = telemetryMapper.toEntity(telemetryCreateRequest);
       Equipament equipament = equipamentRepository.findById(telemetryCreateRequest.equipamentId())
                       .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryCreateRequest.equipamentId()));
       telemetry.setEquipamentId(equipament);
       TelemetryTripLinkResolver.TripLink link = tripLinkResolver.resolve(telemetryCreateRequest.shipmentId(), telemetryCreateRequest.transportId());
       applyTripLink(telemetry, link);
       telemetryRepository.save(telemetry);

       try {
           double lat = Double.parseDouble(telemetryCreateRequest.latitude().trim().replace(',', '.'));
           double lon = Double.parseDouble(telemetryCreateRequest.longitude().trim().replace(',', '.'));
           LocalDateTime ts = telemetryCreateRequest.dateTime() != null ? telemetryCreateRequest.dateTime() : LocalDateTime.now();
           tripTrackingService.track(equipament, link.transport(), lat, lon, ts);
           geofencingService.checkGeofenceArrival(equipament, lat, lon, ts, telemetryCreateRequest.speed());
       } catch (Exception ignored) {}

       return telemetryMapper.toResponse(telemetry);
    }

    public TelemetryReponse get(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tenantGuard.checkAccess(telemetry.getEquipamentId() != null ? telemetry.getEquipamentId().getCompany() : null, id);
        return telemetryMapper.toResponse(telemetry);
    }

    public List<TelemetryResponseList> getAll(){
        return getAll(null, null);
    }

    /** Lista as leituras, opcionalmente filtradas pela viagem (transporte) e/ou remessa, em ordem cronológica. */
    public List<TelemetryResponseList> getAll(UUID transportId, UUID shipmentId){
        List<Telemetry> source;
        if (transportId != null && shipmentId != null) {
            source = telemetryRepository.findByTransportIdIdAndShipmentIdIdOrderByDateTimeAsc(transportId, shipmentId);
        } else if (transportId != null) {
            source = telemetryRepository.findByTransportIdIdOrderByDateTimeAsc(transportId);
        } else if (shipmentId != null) {
            source = telemetryRepository.findByShipmentIdIdOrderByDateTimeAsc(shipmentId);
        } else {
            source = telemetryRepository.findAll();
        }
        List<Telemetry> telemetries = source.stream()
                .filter(t -> t.getEquipamentId() != null && tenantGuard.canSee(t.getEquipamentId().getCompany()))
                .toList();
        return telemetryMapper.toResponseList(telemetries);
    }

    @Transactional
    public void delete(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        telemetryRepository.delete(telemetry);
    }

    @Transactional
    public TelemetryReponse update(UUID id, TelemetryCreateRequest telemetryCreateRequest){
        telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Telemetry telemetry = telemetryMapper.toEntity(telemetryCreateRequest);

        Equipament equipament = equipamentRepository.findById(telemetryCreateRequest.equipamentId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryCreateRequest.equipamentId()));

        telemetry.setId(id);
        telemetry.setEquipamentId(equipament);
        applyTripLink(telemetry, tripLinkResolver.resolve(telemetryCreateRequest.shipmentId(), telemetryCreateRequest.transportId()));
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

    @Transactional
    public TelemetryReponse updatePartial(UUID id, TelemetryUpdateRequest telemetryUpdateRequest){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(telemetryUpdateRequest.alert() != null) telemetry.setAlert(telemetryUpdateRequest.alert());
        if(telemetryUpdateRequest.data1() != null) telemetry.setData1(telemetryUpdateRequest.data1());
        if(telemetryUpdateRequest.data2() != null) telemetry.setData2(telemetryUpdateRequest.data2());
        if(telemetryUpdateRequest.device() != null) telemetry.setDevice(telemetryUpdateRequest.device());
        if(telemetryUpdateRequest.speed() != null) telemetry.setSpeed(telemetryUpdateRequest.speed());
        if(telemetryUpdateRequest.dateTime() != null) telemetry.setDateTime(telemetryUpdateRequest.dateTime());
        if(telemetryUpdateRequest.latitude() != null && !telemetryUpdateRequest.latitude().isBlank())
            telemetry.setLatitude(telemetryUpdateRequest.latitude());
        if(telemetryUpdateRequest.longitude() != null && !telemetryUpdateRequest.longitude().isBlank())
            telemetry.setLongitude(telemetryUpdateRequest.longitude());
        if(telemetryUpdateRequest.equipamentId() != null){
            Equipament equipament = equipamentRepository.findById(telemetryUpdateRequest.equipamentId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryUpdateRequest.equipamentId()));
            telemetry.setEquipamentId(equipament);
        }
        if(telemetryUpdateRequest.shipmentId() != null || telemetryUpdateRequest.transportId() != null){
            // Campo omitido no PATCH mantém o vínculo atual, que também entra na validação de consistência
            UUID shipmentId = telemetryUpdateRequest.shipmentId() != null ? telemetryUpdateRequest.shipmentId()
                    : (telemetry.getShipmentId() != null ? telemetry.getShipmentId().getId() : null);
            UUID transportId = telemetryUpdateRequest.transportId() != null ? telemetryUpdateRequest.transportId()
                    : (telemetryUpdateRequest.shipmentId() != null ? null
                    : (telemetry.getTransportId() != null ? telemetry.getTransportId().getId() : null));
            applyTripLink(telemetry, tripLinkResolver.resolve(shipmentId, transportId));
        }
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

    private static void applyTripLink(Telemetry telemetry, TripLink link){
        telemetry.setShipmentId(link.shipment());
        telemetry.setTransportId(link.transport());
    }
}
