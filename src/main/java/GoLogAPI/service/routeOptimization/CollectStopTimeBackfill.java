package GoLogAPI.service.routeOptimization;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TransportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Corrige, uma vez, as viagens planejadas antes de a coleta com várias entregas filhas contar a parada uma vez só.
 *
 * <p>O otimizador recebia a coleta com N filhas como N visitas de coleta no mesmo endereço, cada uma com a duração da
 * parada; o tempo de operação, o custo de horas e o custo total contavam a coleta N vezes, e (antes da correção da
 * tela) cada visita virava uma parada repetida. Para cada viagem ainda não corrigida:</p>
 * <ol>
 *     <li>junta paradas seguidas da mesma remessa (somando trechos e mantendo a chegada mais cedo);</li>
 *     <li>visitas extras = Σ (entregas filhas da coleta na viagem − 1);</li>
 *     <li>duração por visita = (tempo de operação − tempo de deslocamento) ÷ visitas do plano — a mesma que foi usada;</li>
 *     <li>tempo de operação −= extras × duração; custo de horas recalculado pela mesma tarifa/hora; custo total ajustado.</li>
 * </ol>
 * <p>Marca a viagem ({@code collectStopTimeFixed}); viagens novas já nascem marcadas. Falha de uma viagem não impede as
 * demais nem a subida da API.</p>
 */
@Component
public class CollectStopTimeBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CollectStopTimeBackfill.class);

    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;
    private final TransactionTemplate tx;

    public CollectStopTimeBackfill(TransportRepository transportRepository, RouteStopRepository routeStopRepository,
                                   TransactionTemplate tx) {
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
        this.tx = tx;
    }

    /** Resultado da correção de uma viagem (para log e teste). */
    record Fix(List<RouteStop> keep, List<RouteStop> remove, int extraVisits, Integer newTotalTime,
               Double newCostHour, Double newTotalCost, int shipmentQuantity) {
        boolean changesSomething(Transport t) {
            return !remove.isEmpty() || extraVisits > 0;
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        List<UUID> ids;
        try {
            ids = transportRepository.findAll().stream()
                    .filter(t -> !Boolean.TRUE.equals(t.getCollectStopTimeFixed()))
                    .map(Transport::getId).toList();
        } catch (Exception e) {
            log.error("Tempo da coleta: falha ao listar viagens", e);
            return;
        }
        int fixed = 0;
        for (UUID id : ids) {
            try {
                Boolean changed = tx.execute(status -> transportRepository.findById(id).map(this::fixOne).orElse(false));
                if (Boolean.TRUE.equals(changed)) fixed++;
            } catch (Exception e) {
                log.error("Tempo da coleta: falha ao corrigir a viagem {}", id, e);
            }
        }
        if (!ids.isEmpty()) {
            log.info("Tempo da coleta: {} de {} viagens não marcadas tiveram tempo/custo calculado corrigido", fixed, ids.size());
        }
    }

    private boolean fixOne(Transport t) {
        List<RouteStop> stops = new ArrayList<>(routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(t.getId()));
        Fix fix = compute(t, stops);
        boolean changed = fix.changesSomething(t);
        if (changed) {
            if (!fix.remove().isEmpty()) {
                routeStopRepository.deleteAll(fix.remove());
                routeStopRepository.flush();
                routeStopRepository.saveAll(fix.keep());
            }
            log.info("Tempo da coleta: viagem #{} — {} parada(s) repetida(s) removida(s), {} visita(s) extra(s); "
                            + "tempo {}s → {}s, custo total {} → {}",
                    t.getCodeTransport(), fix.remove().size(), fix.extraVisits(), t.getTotalTimeCalculed(),
                    fix.newTotalTime(), t.getTotalCostCalculed(), fix.newTotalCost());
            t.setTotalTimeCalculed(fix.newTotalTime());
            t.setCostHourCalculed(fix.newCostHour());
            t.setTotalCostCalculed(fix.newTotalCost());
            t.setShipmentQuantity(fix.shipmentQuantity());
        }
        t.setCollectStopTimeFixed(true);
        transportRepository.save(t);
        return changed;
    }

    /** Cálculo puro da correção (sem banco). Altera as paradas mantidas (sequência, trechos somados, chegada). */
    static Fix compute(Transport t, List<RouteStop> stops) {
        stops.sort(Comparator.comparing(rs -> rs.getSequenceOrder() == null ? Integer.MAX_VALUE : rs.getSequenceOrder()));
        int plannedVisits = stops.size(); // antes de juntar: cada visita repetida era uma parada

        List<RouteStop> keep = new ArrayList<>();
        List<RouteStop> remove = new ArrayList<>();
        for (RouteStop rs : stops) {
            RouteStop prev = keep.isEmpty() ? null : keep.get(keep.size() - 1);
            if (prev != null && rs.getShipment() != null && prev.getShipment() != null
                    && Objects.equals(prev.getShipment().getId(), rs.getShipment().getId())) {
                merge(prev, rs);
                remove.add(rs);
            } else {
                keep.add(rs);
            }
        }
        for (int i = 0; i < keep.size(); i++) keep.get(i).setSequenceOrder(i + 1);

        int extra = extraCollectVisits(keep);
        // Paradas já juntas (correção da tela publicada antes desta): o plano tinha as visitas extras sem parada
        if (remove.isEmpty()) plannedVisits = keep.size() + extra;

        Integer total = t.getTotalTimeCalculed();
        Integer travel = t.getTravelDuration();
        Integer newTotal = total;
        Double newCostHour = t.getCostHourCalculed();
        Double newTotalCost = t.getTotalCostCalculed();
        if (extra > 0 && total != null && travel != null && total > travel && plannedVisits > 0) {
            int perVisit = Math.round((float) (total - travel) / plannedVisits);
            newTotal = Math.max(travel, total - extra * perVisit);
            if (t.getCostHourCalculed() != null && total > 0) {
                double ratePerSecond = t.getCostHourCalculed() / total;
                newCostHour = ratePerSecond * newTotal;
                if (newTotalCost != null) newTotalCost = newTotalCost - (t.getCostHourCalculed() - newCostHour);
            }
        }

        Set<UUID> distinct = new HashSet<>();
        keep.forEach(rs -> { if (rs.getShipment() != null) distinct.add(rs.getShipment().getId()); });
        return new Fix(keep, remove, extra, newTotal, newCostHour, newTotalCost, distinct.size());
    }

    /** Σ (entregas filhas da coleta presentes na viagem − 1). */
    static int extraCollectVisits(List<RouteStop> stops) {
        Set<UUID> collects = new HashSet<>();
        Map<UUID, Integer> children = new HashMap<>();
        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null) continue;
            if (s.getTypeOperation() == TypeOperation.COLETA) collects.add(s.getId());
        }
        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null || s.getTypeOperation() != TypeOperation.ENTREGA || s.getOperationOrigem() == null) continue;
            UUID parent = s.getOperationOrigem().getId();
            if (collects.contains(parent)) children.merge(parent, 1, Integer::sum);
        }
        return children.values().stream().mapToInt(n -> Math.max(0, n - 1)).sum();
    }

    private static void merge(RouteStop kept, RouteStop dup) {
        kept.setCalculatedDistance(nz(kept.getCalculatedDistance()) + nz(dup.getCalculatedDistance()));
        kept.setCalculatedDuration(nz(kept.getCalculatedDuration()) + nz(dup.getCalculatedDuration()));
        kept.setCalculatedWait(nz(kept.getCalculatedWait()) + nz(dup.getCalculatedWait()));
        if (dup.getArrivedAt() != null && (kept.getArrivedAt() == null || dup.getArrivedAt().isBefore(kept.getArrivedAt()))) {
            kept.setArrivedAt(dup.getArrivedAt());
            kept.setAutoCheckIn(dup.getAutoCheckIn());
            kept.setCheckInDistance(dup.getCheckInDistance());
            kept.setStatus(dup.getStatus());
        }
        if (kept.getDepartedAt() == null) kept.setDepartedAt(dup.getDepartedAt());
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
