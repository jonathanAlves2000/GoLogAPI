package GoLogAPI.service;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.dto.typeTransport.TypeTransportUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.TypeTransportMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.TypeTransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TypeTransportService {

    private final TypeTransportRepository typeTransportRepository;
    private final TypeTransportMapper typeTransportMapper;
    private final CompanyRepository companyRepository;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public TypeTransportService(TypeTransportRepository typeTransportRepository,
                                TypeTransportMapper typeTransportMapper,
                                CompanyRepository companyRepository,
                                GoLogAPI.infra.tenant.TenantGuard tenantGuard){
        this.tenantGuard = tenantGuard;
        this.typeTransportRepository = typeTransportRepository;
        this.typeTransportMapper = typeTransportMapper;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public TypeTransportResponse save(TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransport typeTransport = typeTransportMapper.toEntity(typeTransportCreateRequest);
        // descrição e cuidados são opcionais na API, mas NOT NULL no banco
        if (typeTransport.getDescription() == null) typeTransport.setDescription("");
        if (typeTransport.getCare() == null) typeTransport.setCare("");
        typeTransport.setCompany(tenantGuard.ownerForCatalog(typeTransportCreateRequest.companyId()));
        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    public TypeTransportResponse get(UUID id){
        TypeTransport typeTransport = find(id);
        tenantGuard.checkCatalogRead(typeTransport.getCompany(), id);
        return typeTransportMapper.toResponse(typeTransport);
    }

    public List<TypeTransportResponse> getAll() {
        UUID currentTenant = TenantContext.getCurrentTenantId();
        List<TypeTransport> typeTransports = typeTransportRepository.findAvailableForCompany(currentTenant);
        return typeTransportMapper.toResponses(typeTransports);
    }

    @Transactional
    public void delete(UUID id){
       TypeTransport typeTransport = find(id);
       tenantGuard.checkCatalogWrite(typeTransport.getCompany(), id);
       typeTransportRepository.delete(typeTransport);
    }

    @Transactional
    public TypeTransportResponse update(UUID id, TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransport typeTransport = find(id);
        tenantGuard.checkCatalogWrite(typeTransport.getCompany(), id);
        typeTransport.setName(typeTransportCreateRequest.name());
        typeTransport.setDescription(typeTransportCreateRequest.description() != null ? typeTransportCreateRequest.description() : "");
        typeTransport.setCare(typeTransportCreateRequest.care() != null ? typeTransportCreateRequest.care() : "");
        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }

    @Transactional
    public TypeTransportResponse updatePartial(UUID id, TypeTransportUpdateRequest typeTransportUpdateRequest){
        TypeTransport typeTransport = find(id);
        tenantGuard.checkCatalogWrite(typeTransport.getCompany(), id);

        if(typeTransportUpdateRequest.name() != null && !typeTransportUpdateRequest.name().isBlank())
            typeTransport.setName(typeTransportUpdateRequest.name());
        if(typeTransportUpdateRequest.description() != null)
            typeTransport.setDescription(typeTransportUpdateRequest.description());
        if(typeTransportUpdateRequest.care() != null)
            typeTransport.setCare(typeTransportUpdateRequest.care());

        typeTransportRepository.save(typeTransport);
        return typeTransportMapper.toResponse(typeTransport);
    }


    private TypeTransport find(UUID id) {
        return typeTransportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de transporte não encontrado: ", id));
    }
}
