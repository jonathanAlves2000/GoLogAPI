package GoLogAPI.service;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorPatchRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TractorMapper;
import GoLogAPI.model.Tractor;
import GoLogAPI.repository.TractorRepository;
import GoLogAPI.validation.TractorValidator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TractorService {

    private TractorRepository tractorRepository;
    private TractorMapper tractorMapper;
    private TractorValidator tractorValidator;

    String message = "Registro não encontrado para o Id: ";

    public TractorService(TractorRepository tractorRepository, TractorMapper tractorMapper, TractorValidator tractorValidator){
        this.tractorRepository = tractorRepository;
        this.tractorMapper = tractorMapper;
        this.tractorValidator = tractorValidator;
    }

    public TractorResponse save(TractorCreateRequest tractorCreateRequest){
        tractorValidator.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public TractorResponse get(UUID id){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        return tractorMapper.toResponse(tractor);
    }

    public void delete(UUID id){
        Tractor tractor = tractorRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(message + id));
        tractorRepository.delete(tractor);
    }

    public TractorResponse update(UUID id, TractorCreateRequest tractorCreateRequest){
        tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        tractorValidator.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);
        tractor.setId(id);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public TractorResponse updatePartial(UUID id, TractorPatchRequest tractorPatchRequest){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(tractorPatchRequest.plate() != null) tractor.setPlate(tractorPatchRequest.plate());
        if(tractorPatchRequest.renavam() != null) tractor.setRenavam(tractorPatchRequest.renavam());
        if(tractorPatchRequest.model() != null) tractor.setModel(tractorPatchRequest.model());
        if(tractorPatchRequest.maximumCapacity() != null) tractor.setMaximumCapacity(tractorPatchRequest.maximumCapacity());
        if(tractorPatchRequest.namberAxles() != null) tractor.setNamberAxles(tractorPatchRequest.namberAxles());
        if(tractorPatchRequest.typeFuel() != null) tractor.setTypeFuel(tractorPatchRequest.typeFuel());

        tractor.setId(id);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }
}
