package GoLogAPI.service;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorUpdateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.TractorMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.Tractor;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.TractorRepository;
import GoLogAPI.validation.TractorValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TractorService {

    private final TractorRepository tractorRepository;
    private final TractorMapper tractorMapper;
    private final TractorValidate tractorValidate;
    private final CompanyRepository companyRepository;

    public TractorService(TractorRepository tractorRepository, TractorMapper tractorMapper,
                          TractorValidate tractorValidate, CompanyRepository companyRepository){
        this.tractorRepository = tractorRepository;
        this.tractorMapper = tractorMapper;
        this.tractorValidate = tractorValidate;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public TractorResponse save(TractorCreateRequest tractorCreateRequest){
        tractorValidate.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);

        Company company = companyRepository.findById(tractorCreateRequest.companyId())
                        .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, tractorCreateRequest.companyId()));

        tractor.setCompany(company);

//        Double litersPerKm = 1.0 / tractorCreateRequest.kmPerLiter();
//        Double factorEmission;
//        Double co2EmissionPerKm;
//
//        factorEmission = switch (tractor.getTypeFuel()) {
//            case DIESEL -> 2.68;
//            case ETANOL -> 1.52;
//            case GASOLINA -> 2.28;
//            default -> 0.0;
//        };
//
//        co2EmissionPerKm = Math.round(factorEmission * litersPerKm * 100.0) / 100.0;
//        tractor.setCo2PerKm(co2EmissionPerKm);

        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }

    public TractorResponse get(UUID id){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return tractorMapper.toResponse(tractor);
    }

    public List<TractorResponse> getAll() {
        List<Tractor> tractors = tractorRepository.findAll();
        return tractorMapper.toResponses(tractors);
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

        Company company = companyRepository.findById(tractorCreateRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, tractorCreateRequest.companyId()));

        //tractorValidator.validate(tractorCreateRequest);
        Tractor tractor = tractorMapper.toEntity(tractorCreateRequest);

//        Double litersPerKm = 1.0 / tractorCreateRequest.kmPerLiter();
//        Double factorEmission;
//        Double co2EmissionPerKm;
//
//        factorEmission = switch (tractor.getTypeFuel()) {
//            case DIESEL -> 2.68;
//            case ETANOL -> 1.52;
//            case GASOLINA -> 2.28;
//            default -> 0.0;
//        };
//
//        co2EmissionPerKm = Math.round(factorEmission * litersPerKm * 100.0) / 100.0;
//        tractor.setCo2PerKm(co2EmissionPerKm);

        tractor.setId(id);
        tractor.setCompany(company);
        tractorRepository.save(tractor);

        return tractorMapper.toResponse(tractor);
    }

    @Transactional
    public TractorResponse updatePartial(UUID id, TractorUpdateRequest tractorUpdateRequest){
        Tractor tractor = tractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        //tractorValidator.validate(tractorUpdateRequest);

        if(tractorUpdateRequest.plate() != null && !tractorUpdateRequest.plate().isBlank())
            tractor.setPlate(tractorUpdateRequest.plate());
        if(tractorUpdateRequest.renavam() != null && !tractorUpdateRequest.renavam().isBlank())
            tractor.setRenavam(tractorUpdateRequest.renavam());
        if(tractorUpdateRequest.model() != null && !tractorUpdateRequest.model().isBlank())
            tractor.setModel(tractorUpdateRequest.model());
        if(tractorUpdateRequest.maximumCapacity() != null)
            tractor.setMaximumCapacity(tractorUpdateRequest.maximumCapacity());
        if(tractorUpdateRequest.numberAxles() != null)
            tractor.setNumberAxles(tractorUpdateRequest.numberAxles());
        if(tractorUpdateRequest.costPerKilometer() != null) {
            tractor.setCostPerKilometer(tractorUpdateRequest.costPerKilometer());

//            Double litersPerKm = 1.0 / tractorUpdateRequest.kmPerLiter();
//            Double factorEmission;
//            Double co2EmissionPerKm;
//
//            factorEmission = switch (tractor.getTypeFuel()) {
//                case DIESEL -> 2.68;
//                case ETANOL -> 1.52;
//                case GASOLINA -> 2.28;
//                default -> 0.0;
//            };

//            co2EmissionPerKm = Math.round(factorEmission * litersPerKm * 100.0) / 100.0;
//            tractor.setCo2PerKm(co2EmissionPerKm);
        }

        if(tractorUpdateRequest.companyId() != null) {
            Company company = companyRepository.findById(tractorUpdateRequest.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, tractorUpdateRequest.companyId()));
            tractor.setCompany(company);
        }
        tractorRepository.save(tractor);
        return tractorMapper.toResponse(tractor);
    }
}
