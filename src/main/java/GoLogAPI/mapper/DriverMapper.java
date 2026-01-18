package GoLogAPI.mapper;

import GoLogAPI.dto.DriverDto;
import GoLogAPI.model.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Driver toEntity(DriverDto driverDTO);

    @Mapping(source = "user.id", target = "userId")
//    @Mapping(source = "driver.id", target = "id")
    DriverDto toDto(Driver driver);
}