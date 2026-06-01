package GoLogAPI.mapper;

import GoLogAPI.dto.occurrence.OccurrenceCreateRequest;
import GoLogAPI.dto.occurrence.OccurrenceCreateResponse;
import GoLogAPI.dto.occurrence.OccurrenceResponse;
import GoLogAPI.dto.occurrence.OccurrenceResponseList;
import GoLogAPI.model.Occurrence;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {Shipment.class, Transport.class, User.class})
public interface OccurrenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shipment", ignore = true)
    @Mapping(target = "transport", ignore = true)
    @Mapping(target = "sender", ignore = true)
    Occurrence toEntity(OccurrenceCreateRequest occurrenceCreateRequest);

    OccurrenceResponse toResponse(Occurrence occurrence);

    @Mapping(target = "shipmentId", source = "shipment.id")
    @Mapping(target = "transportId", source = "transport.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "createdAt", source = "createdAt")
    OccurrenceCreateResponse toCreateResponse(Occurrence occurrence);

    @Mapping(target = "shipmentId", source = "shipment.id")
    @Mapping(target = "transportId", source = "transport.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "createdAt", source = "createdAt")
    OccurrenceResponseList toResponseListItem(Occurrence occurrence);

    List<OccurrenceResponseList> toResponseList(List<Occurrence> occurrences);

}
