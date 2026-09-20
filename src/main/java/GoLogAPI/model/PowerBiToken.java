package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "powerbi_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PowerBiToken {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "id", unique = true, nullable = false)
        private UUID id;

        @Column(name = "jwt_id", unique = true, nullable = false)
        private String jwtId;

        @Column(name = "active", nullable = false)
        private Boolean active;

        @Column(name = "created_at")
        private LocalDateTime createdAt;
}
