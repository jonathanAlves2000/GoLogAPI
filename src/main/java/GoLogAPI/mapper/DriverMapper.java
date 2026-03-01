package GoLogAPI.mapper;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Driver toEntity(DriverCreateRequest driverCreateRequest);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "cpf", source = "user.cpf")
    DriverResponse toResponse(Driver driver);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "cpf", source = "user.cpf")
    DriverResponseList toResponseItem(Driver driver);

    List<DriverResponseList> toResponses(List<Driver> drivers);
}