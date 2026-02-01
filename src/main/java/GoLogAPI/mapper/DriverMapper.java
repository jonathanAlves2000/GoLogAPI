package GoLogAPI.mapper;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Driver toEntity(DriverCreateRequest driverCreateRequest);

    DriverResponse toResponse(Driver driver);
}