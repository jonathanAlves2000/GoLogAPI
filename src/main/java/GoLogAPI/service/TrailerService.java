package GoLogAPI.service;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerUpdateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TrailerMapper;
import GoLogAPI.model.Trailer;
import GoLogAPI.repository.TrailerRepository;
import GoLogAPI.validation.TrailerValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TrailerService {

    private final TrailerRepository trailerRepository;
    private final TrailerMapper trailerMapper;
    private final TrailerValidator trailerValidator;

    public TrailerService(TrailerRepository trailerRepository,
                          TrailerMapper trailerMapper, TrailerValidator trailerValidator){
        this.trailerRepository = trailerRepository;
        this.trailerMapper = trailerMapper;
        this.trailerValidator = trailerValidator;
    }

    @Transactional
    public TrailerResponse save(TrailerCreateRequest trailerCreateRequest){
        trailerValidator.validate(trailerCreateRequest);
        Trailer trailer = trailerMapper.toEntity(trailerCreateRequest);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public TrailerResponse get(UUID id){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return trailerMapper.toResponse(trailer);
    }

    @Transactional
    public void delete(UUID id){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        trailerRepository.delete(trailer);
    }

    @Transactional
    public TrailerResponse update(UUID id, TrailerCreateRequest trailerCreateRequest) {
        trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        Trailer trailer = trailerMapper.toEntity(trailerCreateRequest);
        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    @Transactional
    public TrailerResponse updatePartial(UUID id, TrailerUpdateRequest trailerUpdateRequest){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        trailerValidator.validate(trailerUpdateRequest);

        if(trailerUpdateRequest.plate() != null && !trailerUpdateRequest.plate().isBlank())
            trailer.setPlate(trailerUpdateRequest.plate());
        if(trailerUpdateRequest.renavam() != null && !trailerUpdateRequest.renavam().isBlank())
            trailer.setRenavam(trailerUpdateRequest.renavam());
        if(trailerUpdateRequest.model() != null && !trailerUpdateRequest.model().isBlank())
            trailer.setModel(trailerUpdateRequest.model());
        if(trailerUpdateRequest.numberAxles() != null)
            trailer.setNumberAxles(trailerUpdateRequest.numberAxles());
        if(trailerUpdateRequest.maximumCapacity() != null)
            trailer.setMaximumCapacity(trailerUpdateRequest.maximumCapacity());

        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);

    }
}
