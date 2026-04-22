package GoLogAPI.service;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.dto.typeTransport.TypeTransportUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TypeTransportMapper;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.repository.TypeTransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TypeTransportService {

    private final TypeTransportRepository typeTransportRepository;
    private final TypeTransportMapper typeTransportMapper;

    public TypeTransportService(TypeTransportRepository typeTransportRepository,
                                TypeTransportMapper typeTransportMapper){
        this.typeTransportRepository = typeTransportRepository;
        this.typeTransportMapper = typeTransportMapper;
    }

    @Transactional
    public TypeTransportResponse save(TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransport typeTransport = typeTransportMapper.toEntity(typeTransportCreateRequest);
        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    public TypeTransportResponse get(UUID id){
        TypeTransport typeTransport = typeTransportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return typeTransportMapper.toResponse(typeTransport);
    }

    @Transactional
    public void delete(UUID id){
       TypeTransport typeTransport = typeTransportRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
       typeTransportRepository.delete(typeTransport);
    }

    @Transactional
    public TypeTransportResponse update(UUID id, TypeTransportCreateRequest typeTransportCreateRequest){
        typeTransportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        TypeTransport typeTransport = typeTransportMapper.toEntity(typeTransportCreateRequest);
        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    @Transactional
    public TypeTransportResponse updatePartial(UUID id, TypeTransportUpdateRequest typeTransportUpdateRequest){
        TypeTransport typeTransport = typeTransportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(typeTransportUpdateRequest.name() != null && typeTransportUpdateRequest.name().isBlank())
            typeTransport.setName(typeTransportUpdateRequest.name());
        if(typeTransportUpdateRequest.description() != null)
            typeTransport.setDescription(typeTransportUpdateRequest.description());
        if(typeTransportUpdateRequest.care() != null)
            typeTransport.setCare(typeTransportUpdateRequest.care());

        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

}


