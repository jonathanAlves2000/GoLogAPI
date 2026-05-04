package GoLogAPI.mapper;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryResponseList;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.model.Telemetry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipamentId", ignore = true)
    Telemetry toEntity(TelemetryCreateRequest telemetryCreateRequest);

    TelemetryReponse toResponse(Telemetry telemetry);

    List<TelemetryResponseList> toResponseList(List<Telemetry> telemetries);
}
