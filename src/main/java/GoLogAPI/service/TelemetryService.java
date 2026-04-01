package GoLogAPI.service;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryPatchRequest;
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
                       .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + telemetryCreateRequest.equipamentId()));
       telemetry.setEquipamentId(equipament);
       telemetryRepository.save(telemetry);
       return telemetryMapper.toResponse(telemetry);
    }

    public TelemetryReponse get(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + id));
        return telemetryMapper.toResponse(telemetry);
    }

    public void delete(UUID id){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + id));
        telemetryRepository.delete(telemetry);
    }

    public TelemetryReponse update(UUID id, TelemetryCreateRequest telemetryCreateRequest){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + id));

        Equipament equipament = equipamentRepository.findById(telemetryCreateRequest.equipamentId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + telemetryCreateRequest.equipamentId()));

        telemetry.setEquipamentId(equipament);
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

    public TelemetryReponse updatePartial(UUID id, TelemetryPatchRequest telemetryPatchRequest){
        Telemetry telemetry = telemetryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + id));

        if(telemetryPatchRequest.alert() != null) telemetry.setAlert(telemetryPatchRequest.alert());
        if(telemetryPatchRequest.data1() != null) telemetry.setData1(telemetryPatchRequest.data1());
        if(telemetryPatchRequest.data2() != null) telemetry.setData2(telemetryPatchRequest.data2());
        if(telemetryPatchRequest.device() != null) telemetry.setDevice(telemetryPatchRequest.device());
        if(telemetryPatchRequest.dateTime() != null) telemetry.setDateTime(telemetryPatchRequest.dateTime());
        if(telemetryPatchRequest.latitude() != null) telemetry.setLatitude(telemetryPatchRequest.latitude());
        if(telemetryPatchRequest.longitude() != null) telemetry.setLongitude(telemetryPatchRequest.longitude());
        if(telemetryPatchRequest.equipamentId() != null){
            Equipament equipament = equipamentRepository.findById(telemetryPatchRequest.equipamentId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + telemetryPatchRequest.equipamentId()));
            telemetry.setEquipamentId(equipament);
        }
        telemetryRepository.save(telemetry);
        return telemetryMapper.toResponse(telemetry);
    }

}
