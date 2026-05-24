package GoLogAPI.dto.collect;

import java.util.UUID;

public record CollectResponseList(
    UUID id,
    Integer sequence,
    UUID collectionAddressId,
    UUID customerCollectsId
) { }
