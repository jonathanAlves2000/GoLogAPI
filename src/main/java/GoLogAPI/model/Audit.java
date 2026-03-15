package GoLogAPI.model;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.UUID;

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
        createdAt = Instant.now();
        createdBy = getUserAuthenticatedId();
        updatedAt = this.createdAt;
        updatedBy = getUserAuthenticatedName();
    }

    @PreUpdate
    public void onPreUpdate(){
        updatedAt = Instant.now();
        updatedBy = getUserAuthenticatedName();
    }

    private String getUserAuthenticatedId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
            return null;
        }

        User userLogin = (User) auth.getPrincipal();
        return userLogin.getName();
    }

    private String getUserAuthenticatedName(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userLogin = (User) auth.getPrincipal();
        return (auth != null) ? userLogin.getName() : "System";
    }
}
