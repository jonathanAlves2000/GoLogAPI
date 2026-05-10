package GoLogAPI.service;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.OccurrenceMapper;
import GoLogAPI.model.*;
import GoLogAPI.repository.DeliveryRepository;
import GoLogAPI.repository.OccurrenceRepository;
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private final OccurrenceRepository occurrenceRepository;
    private final DeliveryRepository deliveryRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final OccurrenceMapper occurrenceMapper;

    public OccurrenceService(
            OccurrenceRepository occurrenceRepository, OccurrenceMapper occurrenceMapper,
            DeliveryRepository deliveryRepository, UserRepository userRepository,
            TransportRepository transportRepository)
    {
        this.occurrenceRepository = occurrenceRepository;
        this.occurrenceMapper = occurrenceMapper;
        this.transportRepository = transportRepository;
        this.userRepository = userRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public OccurrenceCreateResponse save(OccurrenceCreateRequest occurrenceCreateRequest){

        Delivery delivery = deliveryRepository.findById(occurrenceCreateRequest.deliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.deliveryId()));

        Transport transport = transportRepository.findById(occurrenceCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.transportId()));

        User sender = userRepository.findById(occurrenceCreateRequest.senderId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.senderId()));

        Occurrence occurrence = occurrenceMapper.toEntity(occurrenceCreateRequest);

        occurrence.setDelivery(delivery);
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

        Delivery delivery = deliveryRepository.findById(occurrenceCreateRequest.deliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.deliveryId()));

        Transport transport = transportRepository.findById(occurrenceCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.transportId()));

        User sender = userRepository.findById(occurrenceCreateRequest.senderId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceCreateRequest.senderId()));

        Occurrence occurrence = occurrenceMapper.toEntity(occurrenceCreateRequest);
        occurrence.setDelivery(delivery);
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

        if(occurrenceUpdateRequest.deliveryId() != null){
            Delivery delivery = deliveryRepository.findById(occurrenceUpdateRequest.deliveryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, occurrenceUpdateRequest.deliveryId()));

            occurrence.setDelivery(delivery);
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
