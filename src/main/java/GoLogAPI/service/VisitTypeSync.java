package GoLogAPI.service;

import GoLogAPI.model.TypeTransport;
import GoLogAPI.model.VisitType;
import GoLogAPI.repository.TypeTransportRepository;
import GoLogAPI.repository.VisitTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Mantém um rótulo de visita (VisitType) para cada tipo de transporte, com o mesmo código que o otimizador
 * envia como {@code shipment_type} ao Google.
 *
 * <p>As regras de incompatibilidade/exigência são cadastradas entre rótulos; o otimizador classifica cada
 * remessa pelo código do seu tipo de transporte. Sem este espelho, uma regra só funcionava se alguém
 * cadastrasse à mão um rótulo com o código idêntico ao nome normalizado do tipo — na prática, nunca.</p>
 */
@Component
public class VisitTypeSync {

    private static final Logger log = LoggerFactory.getLogger(VisitTypeSync.class);

    private final VisitTypeRepository visitTypeRepository;
    private final TypeTransportRepository typeTransportRepository;

    public VisitTypeSync(VisitTypeRepository visitTypeRepository, TypeTransportRepository typeTransportRepository) {
        this.visitTypeRepository = visitTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
    }

    /** Garante o rótulo do tipo no mesmo escopo (padrão do sistema ou tenant). */
    @Transactional
    public void ensureFor(TypeTransport type) {
        String code = type.code();
        if (code.isBlank()) return;
        UUID owner = type.getCompany() != null ? type.getCompany().getId() : null;
        boolean exists = visitTypeRepository.findAll().stream()
                .anyMatch(v -> code.equalsIgnoreCase(v.getCode())
                        && Objects.equals(v.getCompany() != null ? v.getCompany().getId() : null, owner));
        if (exists) return;
        VisitType visitType = new VisitType();
        visitType.setCode(code);
        visitType.setName(type.getName());
        visitType.setDescription("Rótulo automático do tipo de transporte " + type.getName());
        visitType.setCompany(type.getCompany());
        visitTypeRepository.save(visitType);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncAll() {
        int before = (int) visitTypeRepository.count();
        typeTransportRepository.findAll().forEach(this::ensureFor);
        int created = (int) visitTypeRepository.count() - before;
        if (created > 0) log.info("Rótulos de visita criados para tipos de transporte: {}", created);
    }
}
