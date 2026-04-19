package GoLogAPI.dto.supportedType;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.model.EquipamentGroup;

public record SupportedTypeResponse(
    EquipamentGroupResponse equipamentGroup,
    TypeTransportResponse typeTransport
) { }
