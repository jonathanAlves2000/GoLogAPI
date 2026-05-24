package GoLogAPI.service;

import GoLogAPI.dto.collect.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.CollectMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Collect;
import GoLogAPI.model.Company;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CollectRepository;
import GoLogAPI.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CollectService {

    private final CollectRepository collectRepository;
    private final CollectMapper collectMapper;
    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;

    public CollectService(CollectRepository collectRepository, CollectMapper collectMapper,
                          AddressRepository addressRepository, CompanyRepository companyRepository){
        this.collectRepository = collectRepository;
        this.collectMapper =collectMapper;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CollectCreateResponse save(CollectCreateRequest collectCreateRequest){
        Collect collect = collectMapper.toEntity(collectCreateRequest);

        Address address = addressRepository.findById(collectCreateRequest.collectionAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectCreateRequest.customerCollectsId()));

        Company company = companyRepository.findById(collectCreateRequest.customerCollectsId())
                .orElseThrow(() -> new ResourceNotFoundException((MessageException.NOT_FOUND_MESSAGE), collectCreateRequest.customerCollectsId()));

        collect.setCollectionAddress(address);
        collect.setCustomerCollects(company);
        collectRepository.save(collect);

        return collectMapper.toCreateResponse(collect);
    }

    public CollectResponse get(UUID id){
        Collect collect = collectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return collectMapper.toResponse(collect);
    }

    public List<CollectResponseList> getAll(){
        List<Collect> collects = collectRepository.findAll();
        return collectMapper.toResponses(collects);
    }

    @Transactional
    public void delete(UUID id){
        Collect collect = collectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        collectRepository.delete(collect);
    }

    @Transactional
    public CollectCreateResponse update(UUID id, CollectCreateRequest collectCreateRequest){
        Collect collect = collectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Address address = addressRepository.findById(collectCreateRequest.collectionAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectCreateRequest.customerCollectsId()));

        Company company = companyRepository.findById(collectCreateRequest.customerCollectsId())
                .orElseThrow(() -> new ResourceNotFoundException((MessageException.NOT_FOUND_MESSAGE), collectCreateRequest.customerCollectsId()));

        collect.setCollectionAddress(address);
        collect.setCustomerCollects(company);
        collect.setSequence(collectCreateRequest.sequence());

        collectRepository.save(collect);
        return collectMapper.toCreateResponse(collect);
    }

    @Transactional
    public CollectCreateResponse updatePartial(UUID id, CollectUpdateRequest collectUpdateRequest){
        Collect collect = collectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(collectUpdateRequest.sequence() != null)
            collect.setSequence(collectUpdateRequest.sequence());

        if(collectUpdateRequest.collectionAddressId() != null){
            Address address = addressRepository.findById(collectUpdateRequest.collectionAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectUpdateRequest.customerCollectsId()));
            collect.setCollectionAddress(address);
        }

        if(collectUpdateRequest.customerCollectsId() != null){
            Company company = companyRepository.findById(collectUpdateRequest.customerCollectsId())
                    .orElseThrow(() -> new ResourceNotFoundException((MessageException.NOT_FOUND_MESSAGE), collectUpdateRequest.customerCollectsId()));
            collect.setCustomerCollects(company);
        }

        collectRepository.save(collect);
        return collectMapper.toCreateResponse(collect);
    }
}
