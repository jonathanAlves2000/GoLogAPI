package GoLogAPI.model;

import GoLogAPI.model.enums.TypeFuel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tractor_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tractor extends Equipament{

    @Enumerated(EnumType.STRING)
    @Column(name = "type_fuel", nullable = false)
    private TypeFuel typeFuel;

    @Column(name = "cost_per_kilometer", nullable = false)
    private Double costPerKilometer;

    @Column(name = "co2_per_kilometer")
    private Double co2PerKilometer;

    /** Categoria do veículo; nulo em cadastros legados é tratado como cavalo mecânico. */
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private GoLogAPI.model.enums.VehicleType vehicleType;

    /** Volume útil do baú/carroceria (m³) para veículos rígidos; cavalos usam o volume das carretas. */
    @Column(name = "cargo_volume")
    private Double cargoVolume;

    public GoLogAPI.model.enums.VehicleType effectiveVehicleType() {
        return vehicleType != null ? vehicleType : GoLogAPI.model.enums.VehicleType.CAVALO_MECANICO;
    }

}
