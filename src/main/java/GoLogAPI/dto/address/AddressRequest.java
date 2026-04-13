package GoLogAPI.dto.address;

public interface AddressRequest {
    String street();
    String number();
    String district();
    String city();
    String state();
    String country();
    String cep();
    String complement();
}
