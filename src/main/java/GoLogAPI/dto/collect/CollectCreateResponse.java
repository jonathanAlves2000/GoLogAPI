package GoLogAPI.dto.collect;

import java.util.UUID;

public record CollectCreateResponse(
        UUID id,
        Integer sequence,
        UUID collectionAddressId,
        UUID customerCollectsId
) { }