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

import java.util.UUID;

@Service
public class TrailerService {

    TrailerRepository trailerRepository;
    TrailerMapper trailerMapper;
    TrailerValidator trailerValidator;

    public TrailerService(TrailerRepository trailerRepository,
                          TrailerMapper trailerMapper, TrailerValidator trailerValidator){
        this.trailerRepository = trailerRepository;
        this.trailerMapper = trailerMapper;
        this.trailerValidator = trailerValidator;
    }

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

    public void delete(UUID id){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        trailerRepository.delete(trailer);
    }

    public TrailerResponse update(UUID id, TrailerCreateRequest trailerCreateRequest) {
        trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        Trailer trailer = trailerMapper.toEntity(trailerCreateRequest);
        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public TrailerResponse updatePartial(UUID id, TrailerUpdateRequest trailerUpdateRequest){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        trailerValidator.validate(trailerUpdateRequest);

        if(trailerUpdateRequest.plate() != null) trailer.setPlate(trailerUpdateRequest.plate());
        if(trailerUpdateRequest.renavam() != null) trailer.setRenavam(trailerUpdateRequest.renavam());
        if(trailerUpdateRequest.model() != null) trailer.setModel(trailerUpdateRequest.model());
        if(trailerUpdateRequest.numberAxles() != null) trailer.setNumberAxles(trailerUpdateRequest.numberAxles());
        if(trailerUpdateRequest.maximumCapacity() != null) trailer.setMaximumCapacity(trailerUpdateRequest.maximumCapacity());

        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);

    }
}
