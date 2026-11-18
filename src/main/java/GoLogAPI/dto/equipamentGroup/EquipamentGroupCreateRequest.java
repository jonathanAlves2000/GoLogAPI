package GoLogAPI.dto.equipamentGroup;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipamentGroupCreateRequest(
        String observation,

        @NotNull
        UUID equipament1Id,

        UUID equipament2Id,

        UUID equipament3Id,

        UUID companyId,

        /**
         * Tipos de transporte para os quais o conjunto está equipado (ex.: baú refrigerado → Refrigerada).
         * Só restringe tipos marcados com "exige veículo compatível". Em edições, {@code null} mantém o que está salvo.
         */
        java.util.List<UUID> typeTransportIds
){}
