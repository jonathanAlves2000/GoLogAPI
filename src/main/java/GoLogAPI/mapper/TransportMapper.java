package GoLogAPI.mapper;

import GoLogAPI.dto.transport.TransportCreateRequest;
import GoLogAPI.dto.transport.TransportCreateResponse;
import GoLogAPI.dto.transport.TransportResponse;
import GoLogAPI.model.Transport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DriverMapper.class, CompanyMapper.class, EquipamentGroupMapper.class})
public interface TransportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "transporter", ignore = true)
    @Mapping(target = "equipamentGroup", ignore = true)
    Transport toEntity(TransportCreateRequest transportCreateRequest);

    TransportResponse toResponse(Transport transport);

    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "transporterId", source = "transporter.id")
    @Mapping(target = "equipamentGroupId", source = "equipamentGroup.id")
    TransportCreateResponse toCreateResponse(Transport transport);
}
