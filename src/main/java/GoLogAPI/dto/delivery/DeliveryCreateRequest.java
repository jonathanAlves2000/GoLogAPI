package GoLogAPI.dto.delivery;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryCreateRequest(

    @NotNull(message = "O peso da entrega não poder nulo.")
    @Positive(message = "O peso da entrega não pode ser menor ou igual a zero.")
    Double weight,

    @NotNull(message = "O volume da entrega não poder nulo.")
    @Positive(message = "O volume da entrega não pode ser menor ou igual a zero.")
    Double volume,

    @NotNull(message = "A ata de coleta não poder nula.")
    @FutureOrPresent(message = "A data de coleta deve ser no futuro ou no presente")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime scheduledCollection,

    @NotNull(message = "A ata de entrega não poder nula.")
    @FutureOrPresent(message = "A data de entrega deve ser no futuro ou no presente")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime scheduledDelivery,

    @NotBlank(message = "A rota planejada não pode estar vazia.")
    @Size(min = 5, max = 10000, message = "A rota planejada precisa conter entre 5 e 10000 caracteres.")
    String routePlanned,

    @NotBlank(message = "A rota realizada não pode estar vazia.")
    @Size(min = 5, max = 10000, message = "A rota realizada precisa conter entre 5 e 10000 caracteres.")
    String routeCompleted,

    @NotBlank(message = "O status da rota não pode estar vazio.")
    String status,

    @NotNull(message = "A sequencia da rota não pode ser nula.")
    Integer deliverySequence,

    @NotNull(message = "O Id so usuario não pode ser nulo.")
    UUID userId,

    @NotNull(message = "O Id do tipo de entrega não pode ser nulo.")
    UUID deliveryTypeId,

    @NotNull(message = "O Id do transporte não pode ser nulo.")
    UUID transportId,

    @NotNull(message = "O Id do tipo de transporte não pode ser nulo.")
        UUID typeTransportId,

    @NotNull(message = "O Id do endereço de coleta não pode ser nulo.")
    UUID originAdrressId,

    @NotNull(message = "O Id do endereço de entrega não pode ser nulo.")
    UUID destinationAddressId,

    @NotNull(message = "O Id do cliente de coleta não pode ser nulo.")
    UUID customerCollectsId,

    @NotNull(message = "O Id do cliente de entrega não pode ser nulo.")
    UUID customerDeliveryId
) { }
