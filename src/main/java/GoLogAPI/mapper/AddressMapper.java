package GoLogAPI.mapper;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressCreateRequest addressCreateRequest);

    AddressResponse toResponse(Address address);

    List<AddressResponse> toResponses(List<Address> addresses);
}
