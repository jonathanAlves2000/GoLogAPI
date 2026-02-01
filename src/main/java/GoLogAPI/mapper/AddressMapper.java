package GoLogAPI.mapper;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressCreateRequest addressCreateRequest);

    AddressResponse toDto(Address address);
}
