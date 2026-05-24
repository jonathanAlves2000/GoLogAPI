package GoLogAPI.dto.collect;

import GoLogAPI.model.Address;
import GoLogAPI.model.Company;

import java.util.UUID;

public record CollectResponse(
        UUID id,
        Integer sequence,
        Address collectionAddress,
        Company customerCollects
) { }
