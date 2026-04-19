package GoLogAPI.service;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorUpdateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TractorMapper;
import GoLogAPI.model.Tractor;
import GoLogAPI.repository.TractorRepository;
import GoLogAPI.validation.TractorValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TractorService {

    private TractorRepository tractorRepository;
    private TractorMapper tractorMapper;
    private TractorValidator tractorValidator;

    public TractorService(TractorRepository tractorRepository, TractorMapper tractorMapper, TractorValidator tractorValidator){
        this.tractorRepository = tractorRepository;
        this.tractorMapper = tractorMapper;
        this.tractorValidator = tractorValidator;
    }

    @Transactional
    public TractorResponse save(TractorCreateRequest tractorCreateRequest){
        tractorValidator.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public TractorResponse get(UUID id){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return tractorMapper.toResponse(tractor);
    }

    @Transactional
    public void delete(UUID id){
        Tractor tractor = tractorRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tractorRepository.delete(tractor);
    }

    @Transactional
    public TractorResponse update(UUID id, TractorCreateRequest tractorCreateRequest){
        tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tractorValidator.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);
        tractor.setId(id);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    @Transactional
    public TractorResponse updatePartial(UUID id, TractorUpdateRequest tractorUpdateRequest){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        tractorValidator.validate(tractorUpdateRequest);

        if(tractorUpdateRequest.plate() != null) tractor.setPlate(tractorUpdateRequest.plate());
        if(tractorUpdateRequest.renavam() != null) tractor.setRenavam(tractorUpdateRequest.renavam());
        if(tractorUpdateRequest.model() != null) tractor.setModel(tractorUpdateRequest.model());
        if(tractorUpdateRequest.maximumCapacity() != null) tractor.setMaximumCapacity(tractorUpdateRequest.maximumCapacity());
        if(tractorUpdateRequest.numberAxles() != null) tractor.setNumberAxles(tractorUpdateRequest.numberAxles());
        if(tractorUpdateRequest.typeFuel() != null) tractor.setTypeFuel(tractorUpdateRequest.typeFuel());

        tractor.setId(id);
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }
}
