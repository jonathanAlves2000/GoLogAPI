package GoLogAPI.service;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.OccurrenceMapper;
import GoLogAPI.model.*;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.OccurrenceRepository;
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private final OccurrenceRepository occurrenceRepository;
    private final ShipmentRepository shipmentRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final OccurrenceMapper occurrenceMapper;

    public OccurrenceService(
            OccurrenceRepository occurrenceRepository, OccurrenceMapper occurrenceMapper,
            ShipmentRepository shipmentRepository, UserRepository userRepository,
            TransportRepository transportRepository)
    {
        this.occurrenceRepository = occurrenceRepository;
        this.occurrenceMapper = occurrenceMapper;
        this.transportRepository = transportRepository;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public OccurrenceCreateResponse save(OccurrenceCreateRequest occurrenceCreateRequest){

        Shipment shipment = shipmentRepository.findById(occurrenceCreateRequest.shipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.shipmentId()));

        Transport transport = transportRepository.findById(occurrenceCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.transportId()));

        User sender = userRepository.findById(occurrenceCreateRequest.senderId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.senderId()));

        Occurrence occurrence = occurrenceMapper.toEntity(occurrenceCreateRequest);

        occurrence.setShipment(shipment);
        occurrence.setTransport(transport);
        occurrence.setSender(sender);

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }

    public OccurrenceResponse get(UUID id){
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        return occurrenceMapper.toResponse(occurrence);
    }

    public List<OccurrenceResponseList> getAll(){
        List<Occurrence> occurrences = occurrenceRepository.findAll();
        return occurrenceMapper.toResponseList(occurrences);
    }

    @Transactional
    public void delete(UUID id){
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        occurrenceRepository.delete(occurrence);
    }

    @Transactional
    public OccurrenceCreateResponse update(UUID id, OccurrenceCreateRequest occurrenceCreateRequest){
        occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Shipment shipment = shipmentRepository.findById(occurrenceCreateRequest.shipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.shipmentId()));

        Transport transport = transportRepository.findById(occurrenceCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.transportId()));

        User sender = userRepository.findById(occurrenceCreateRequest.senderId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.senderId()));

        Occurrence occurrence = occurrenceMapper.toEntity(occurrenceCreateRequest);
        occurrence.setShipment(shipment);
        occurrence.setTransport(transport);
        occurrence.setSender(sender);
        occurrence.setId(id);

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }

    @Transactional
    public OccurrenceCreateResponse updatePartial(UUID id, OccurrenceUpdateRequest occurrenceUpdateRequest){
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(occurrenceUpdateRequest.type() != null && !occurrenceUpdateRequest.type().isBlank())
            occurrence.setType(occurrenceUpdateRequest.type());

        if(occurrenceUpdateRequest.description() != null && !occurrenceUpdateRequest.description().isBlank())
            occurrence.setDescription(occurrenceUpdateRequest.description());

        if(occurrenceUpdateRequest.attachment() != null && !occurrenceUpdateRequest.attachment().isBlank())
            occurrence.setAttachment(occurrenceUpdateRequest.attachment());

        if(occurrenceUpdateRequest.shipmentId() != null){
            Shipment shipment = shipmentRepository.findById(occurrenceUpdateRequest.shipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceUpdateRequest.shipmentId()));

            occurrence.setShipment(shipment);
        }

        if(occurrenceUpdateRequest.transportId() != null){
            Transport transport = transportRepository.findById(occurrenceUpdateRequest.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceUpdateRequest.transportId()));

            occurrence.setTransport(transport);
        }

        if(occurrenceUpdateRequest.senderId() != null){
            User sender = userRepository.findById(occurrenceUpdateRequest.senderId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceUpdateRequest.senderId()));

            occurrence.setSender(sender);
        }

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }

}
