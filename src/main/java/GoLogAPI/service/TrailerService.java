package GoLogAPI.service;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerPatchRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TrailerMapper;
import GoLogAPI.model.Trailer;
import GoLogAPI.repository.TrailerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrailerService {

    TrailerRepository trailerRepository;
    TrailerMapper trailerMapper;

    String message = "Registro não encontrado para o Id: ";

    public TrailerService(TrailerRepository trailerRepository, TrailerMapper trailerMapper){
        this.trailerRepository = trailerRepository;
        this.trailerMapper = trailerMapper;
    }

    public TrailerResponse save(TrailerCreateRequest trailerCreateRequest){

        Trailer trailer = trailerMapper.toEntity(trailerCreateRequest);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public TrailerResponse get(UUID id){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        return trailerMapper.toResponse(trailer);
    }

    public void delete(UUID id){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        trailerRepository.delete(trailer);
    }

    public TrailerResponse update(UUID id, TrailerCreateRequest trailerCreateRequest) {
        trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        Trailer trailer = trailerMapper.toEntity(trailerCreateRequest);
        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);
    }

    public TrailerResponse updatePartial(UUID id, TrailerPatchRequest trailerPatchRequest){
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(trailerPatchRequest.plate() != null) trailer.setPlate(trailerPatchRequest.plate());
        if(trailerPatchRequest.renavam() != null) trailer.setRenavam(trailerPatchRequest.renavam());
        if(trailerPatchRequest.model() != null) trailer.setModel(trailerPatchRequest.model());
        if(trailerPatchRequest.namberAxles() != null) trailer.setModel(trailerPatchRequest.model());
        if(trailerPatchRequest.maximumCapacity() != null) trailer.setMaximumCapacity(trailerPatchRequest.maximumCapacity());

        trailer.setId(id);
        trailerRepository.save(trailer);
        return trailerMapper.toResponse(trailer);

    }
}
