package GoLogAPI.dto.address;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String number,
        String district,
        String city,
        String state,
        String country,
        String cep,
        String complement,
        String latitude,
        String longitude
) { }
