package GoLogAPI.service;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.dto.typeTransport.TypeTransportUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TypeTransportMapper;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.repository.TypeTranportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TypeTransportService {

    private final TypeTranportRepository typeTranportRepository;
    private final TypeTransportMapper typeTransportMapper;

    public TypeTransportService(TypeTranportRepository typeTranportRepository,
                                TypeTransportMapper typeTransportMapper){
        this.typeTranportRepository = typeTranportRepository;
        this.typeTransportMapper = typeTransportMapper;
    }

    @Transactional
    public TypeTransportResponse save(TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransport typeTransport = typeTransportMapper.toEntity(typeTransportCreateRequest);
        typeTranportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    public TypeTransportResponse get(UUID id){
        TypeTransport typeTransport = typeTranportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return typeTransportMapper.toResponse(typeTransport);
    }

    @Transactional
    public void delete(UUID id){
       TypeTransport typeTransport = typeTranportRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
       typeTranportRepository.delete(typeTransport);
    }

    @Transactional
    public TypeTransportResponse update(UUID id, TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransport typeTransport = typeTranportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        typeTransport.setId(id);
        typeTranportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    @Transactional
    public TypeTransportResponse updatePartial(UUID id, TypeTransportUpdateRequest typeTransportUpdateRequest){
        TypeTransport typeTransport = typeTranportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(typeTransportUpdateRequest.name() != null) typeTransport.setName(typeTransportUpdateRequest.name());
        if(typeTransportUpdateRequest.description() != null) typeTransport.setDescription(typeTransportUpdateRequest.description());
        if(typeTransportUpdateRequest.care() != null) typeTransport.setCare(typeTransportUpdateRequest.care());

        typeTransport.setId(id);
        typeTranportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

}


