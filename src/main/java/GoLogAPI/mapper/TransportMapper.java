package GoLogAPI.mapper;

import GoLogAPI.dto.occurrence.OccurrenceResponseList;
import GoLogAPI.dto.transport.TransportCreateRequest;
import GoLogAPI.dto.transport.TransportCreateResponse;
import GoLogAPI.dto.transport.TransportResponse;
import GoLogAPI.model.Occurrence;
import GoLogAPI.model.Transport;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DriverMapper.class, CompanyMapper.class, EquipamentGroupMapper.class})
public interface TransportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "transporter", ignore = true)
    @Mapping(target = "equipamentGroup", ignore = true)
    Transport toEntity(TransportCreateRequest transportCreateRequest);

    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "transporterId", source = "transporter.id")
    @Mapping(target = "equipamentGroupId", source = "equipamentGroup.id")
    TransportCreateResponse toCreateResponse(Transport transport);

    @Mapping(target = "transporter", source = "transporter")
    @Mapping(target = "driver", source = "driver")
    @Mapping(target = "equipamentGroup", source = "equipamentGroup")
    TransportResponse toResponse(Transport transport);

    List<TransportResponse> toResponses(List<Transport> transports);
}
