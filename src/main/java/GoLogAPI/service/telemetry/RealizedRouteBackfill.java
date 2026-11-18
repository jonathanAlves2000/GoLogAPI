package GoLogAPI.service.telemetry;

import GoLogAPI.model.RouteStop;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TelemetryRepository;
import GoLogAPI.repository.TransportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Na subida da API, remonta a rota realizada de toda viagem que tem telemetria, usando só os pontos com o
 * {@code transport_id} dela. Corrige viagens gravadas antes da correção (pontos acumulados na viagem mais recente
 * do veículo). Idempotente: só grava a viagem quando algo muda. Falha de uma viagem não impede as demais nem a API.
 */
@Component
public class RealizedRouteBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RealizedRouteBackfill.class);

    private final TelemetryRepository telemetryRepository;
    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;
    private final TripTrackingService tripTrackingService;
    private final TransactionTemplate tx;

    public RealizedRouteBackfill(TelemetryRepository telemetryRepository, TransportRepository transportRepository,
                                 RouteStopRepository routeStopRepository, TripTrackingService tripTrackingService,
                                 TransactionTemplate tx) {
        this.telemetryRepository = telemetryRepository;
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
        this.tripTrackingService = tripTrackingService;
        this.tx = tx;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<UUID> ids;
        try {
            ids = telemetryRepository.findDistinctTransportIds();
        } catch (Exception e) {
            log.error("Rota realizada: falha ao listar viagens com telemetria", e);
            return;
        }
        int updated = 0;
        for (UUID id : ids) {
            try {
                Boolean changed = tx.execute(status -> transportRepository.findById(id).map(t -> {
                    List<RouteStop> stops = routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(id);
                    if (!tripTrackingService.rebuild(t, stops)) return false;
                    transportRepository.save(t);
                    return true;
                }).orElse(false));
                if (Boolean.TRUE.equals(changed)) updated++;
            } catch (Exception e) {
                log.error("Rota realizada: falha ao remontar a viagem {}", id, e);
            }
        }
        log.info("Rota realizada: {} de {} viagens com telemetria remontadas a partir do transport_id", updated, ids.size());
    }
}
