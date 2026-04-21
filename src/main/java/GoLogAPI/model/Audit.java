package GoLogAPI.model;
import java.time.Instant;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class Audit{

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    public void onPrePersist(){
        String username = getUserAuthenticatedName();
        createdAt = Instant.now();
        createdBy = username;
        updatedAt = this.createdAt;
        updatedBy = username;
    }

    @PreUpdate
    public void onPreUpdate(){
        updatedAt = Instant.now();
        updatedBy = getUserAuthenticatedName();
    }

    private String getUserAuthenticatedName(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "System";
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof String) {
            return (String) principal;
        }

        return principal.toString();
    }
}
