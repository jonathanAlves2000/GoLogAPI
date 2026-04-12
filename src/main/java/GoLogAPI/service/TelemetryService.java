package GoLogAPI.service;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryUpdateRequest;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TelemetryMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.Telemetry;
import GoLogAPI.repository.EquipamentRepository;
import GoLogAPI.repository.TelemetryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TelemetryService {

    public TelemetryRepository telemetryRepository;
    public TelemetryMapper telemetryMapper;
    public EquipamentRepository equipamentRepository;

    public TelemetryService(TelemetryRepository telemetryRepository,
                            TelemetryMapper telemetryMapper, EquipamentRepository equipamentRepository){
        this.telemetryRepository = telemetryRepository;
        this.telemetryMapper = telemetryMapper;
        this.equipamentRepository = equipamentRepository;
    }

    public TelemetryReponse save(TelemetryCreateRequest telemetryCreateRequest){
       Telemetry telemetry = telemetryMapper.toEntity(telemetryCreateRequest);
       Equipament equipament = equipamentRepository.findById(telemetryCreateRequest.equipamentId())
                       .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryCreateRequest.equipamentId()));
       telemetry.setEquipamentId(equipament);
       telemetryRepository.save(telemetry);
       return telemetryMapper.toResponse(telemetry);
    }

    public TelemetryReponse get(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return telemetryMapper.toResponse(telemetry);
    }

    public void delete(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        telemetryRepository.delete(telemetry);
    }

    public TelemetryReponse update(UUID id, TelemetryCreateRequest telemetryCreateRequest){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Equipament equipament = equipamentRepository.findById(telemetryCreateRequest.equipamentId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryCreateRequest.equipamentId()));

        telemetry.setEquipamentId(equipament);
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

    public TelemetryReponse updatePartial(UUID id, TelemetryUpdateRequest telemetryUpdateRequest){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(telemetryUpdateRequest.alert() != null) telemetry.setAlert(telemetryUpdateRequest.alert());
        if(telemetryUpdateRequest.data1() != null) telemetry.setData1(telemetryUpdateRequest.data1());
        if(telemetryUpdateRequest.data2() != null) telemetry.setData2(telemetryUpdateRequest.data2());
        if(telemetryUpdateRequest.device() != null) telemetry.setDevice(telemetryUpdateRequest.device());
        if(telemetryUpdateRequest.dateTime() != null) telemetry.setDateTime(telemetryUpdateRequest.dateTime());
        if(telemetryUpdateRequest.latitude() != null) telemetry.setLatitude(telemetryUpdateRequest.latitude());
        if(telemetryUpdateRequest.longitude() != null) telemetry.setLongitude(telemetryUpdateRequest.longitude());
        if(telemetryUpdateRequest.equipamentId() != null){
            Equipament equipament = equipamentRepository.findById(telemetryUpdateRequest.equipamentId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, telemetryUpdateRequest.equipamentId()));
            telemetry.setEquipamentId(equipament);
        }
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

}
