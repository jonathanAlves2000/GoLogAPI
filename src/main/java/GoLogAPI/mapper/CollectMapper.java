package GoLogAPI.mapper;

import GoLogAPI.dto.collect.CollectCreateRequest;
import GoLogAPI.dto.collect.CollectCreateResponse;
import GoLogAPI.dto.collect.CollectResponse;
import GoLogAPI.dto.collect.CollectResponseList;
import GoLogAPI.model.Collect;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, CompanyMapper.class})
public interface CollectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collectionAddress", ignore = true)
    @Mapping(target = "customerCollects", ignore = true)
    Collect toEntity(CollectCreateRequest collectCreateRequest);

    @Mapping(target = "collectionAddressId", source = "collectionAddress.id")
    @Mapping(target = "customerCollectsId", source = "customerCollects.id")
    CollectCreateResponse toCreateResponse(Collect collect);

    CollectResponse toResponse(Collect collect);

    @Mapping(target = "collectionAddressId", source = "collectionAddress.id")
    @Mapping(target = "customerCollectsId", source = "customerCollects.id")
    CollectResponseList toResponseCollect(Collect collect);
    List<CollectResponseList> toResponses(List<Collect> collects);

}