package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Transport;
import GoLogAPI.repository.TransportRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Monta a rota realizada da viagem a partir da telemetria, de forma incremental: cada ponto aceito estende a
 * polilinha {@code routeCompleted}, soma o trecho em {@code distanceTraveled} e soma o t·km do trecho com a carga
 * que estava a bordo. Precisa ficar no backend porque depende de todos os pontos (o front nunca os recebe) e da
 * carga a bordo no instante de cada ponto, que não é reconstruível depois.
 *
 * <p>Só acumula em viagem aberta: a informada pelo app no ponto ou, sem ela, a em execução do veículo (ver
 * {@link ActiveTripResolver}). Pontos fora de ordem, saltos impossíveis (&gt; {@value #MAX_SPEED_KMH} km/h) e ruído de GPS
 * (&lt; {@value #MIN_SEGMENT_METERS} m) são descartados.</p>
 */
@Service
public class TripTrackingService {

    static final double MIN_SEGMENT_METERS = 25.0;
    static final double MAX_SPEED_KMH = 200.0;

    private final TransportRepository transportRepository;
    private final ActiveTripResolver tripResolver;

    public TripTrackingService(TransportRepository transportRepository, ActiveTripResolver tripResolver) {
        this.transportRepository = transportRepository;
        this.tripResolver = tripResolver;
    }

    /** Registra o ponto na viagem em execução do veículo (sem viagem informada). */
    public Transport track(Equipament equipament, double lat, double lon, LocalDateTime timestamp) {
        return track(equipament, null, lat, lon, timestamp);
    }

    /**
     * Registra o ponto na viagem informada pelo app ou, sem ela, na viagem em execução do veículo
     * (critério em {@link ActiveTripResolver}).
     *
     * @return o transporte ao qual o ponto pertence, ou {@code null} se ele não pertence a nenhuma viagem aberta
     */
    public Transport track(Equipament equipament, Transport linked, double lat, double lon, LocalDateTime timestamp) {
        if (timestamp == null) return null;
        ActiveTripResolver.ResolvedTrip trip = tripResolver.resolve(equipament, linked, false);
        if (trip == null) return null;

        Transport transport = trip.transport();
        if (apply(transport, trip.stops(), lat, lon, timestamp)) {
            transportRepository.save(transport);
        }
        return transport;
    }

    static boolean isRunning(List<RouteStop> stops) {
        return ActiveTripResolver.isRunning(stops);
    }

    /** Aplica o ponto ao transporte; devolve {@code true} se algo mudou. */
    static boolean apply(Transport t, List<RouteStop> stops, double lat, double lon, LocalDateTime ts) {
        if (t.getLastTelemetryAt() == null || t.getLastTelemetryLat() == null || t.getLastTelemetryLon() == null) {
            t.setTrackingStartedAt(ts);
            t.setRouteCompleted(encodePoint(lat, lon, 0, 0));
            t.setDistanceTraveled(0);
            t.setTonKmTraveled(0.0);
            setLast(t, lat, lon, ts);
            return true;
        }
        if (!ts.isAfter(t.getLastTelemetryAt())) return false;

        double prevLat = t.getLastTelemetryLat();
        double prevLon = t.getLastTelemetryLon();
        double meters = GeofencingService.calculateHaversineDistanceMeters(prevLat, prevLon, lat, lon);
        if (meters < MIN_SEGMENT_METERS) return false;

        double hours = Duration.between(t.getLastTelemetryAt(), ts).toMillis() / 3_600_000.0;
        if (hours > 0 && (meters / 1000.0) / hours > MAX_SPEED_KMH) return false;

        double tons = TripLoad.onboardKg(stops) / 1000.0;
        int traveled = t.getDistanceTraveled() == null ? 0 : t.getDistanceTraveled();
        double tonKm = t.getTonKmTraveled() == null ? 0.0 : t.getTonKmTraveled();

        t.setDistanceTraveled(traveled + (int) Math.round(meters));
        t.setTonKmTraveled(tonKm + tons * (meters / 1000.0));
        String route = t.getRouteCompleted() == null ? "" : t.getRouteCompleted();
        t.setRouteCompleted(route + encodePoint(lat, lon, prevLat, prevLon));
        setLast(t, lat, lon, ts);
        return true;
    }

    private static void setLast(Transport t, double lat, double lon, LocalDateTime ts) {
        t.setLastTelemetryAt(ts);
        t.setLastTelemetryLat(lat);
        t.setLastTelemetryLon(lon);
    }

    /**
     * Um ponto no formato Encoded Polyline (precisão 1e5) relativo ao anterior. Como o formato é delta-codificado,
     * concatenar pontos assim gera a mesma string que codificar a lista inteira — sem reprocessar o histórico.
     */
    static String encodePoint(double lat, double lon, double prevLat, double prevLon) {
        StringBuilder sb = new StringBuilder();
        encodeSigned(Math.round(lat * 1e5) - Math.round(prevLat * 1e5), sb);
        encodeSigned(Math.round(lon * 1e5) - Math.round(prevLon * 1e5), sb);
        return sb.toString();
    }

    private static void encodeSigned(long value, StringBuilder sb) {
        long v = value < 0 ? ~(value << 1) : (value << 1);
        while (v >= 0x20) {
            sb.append((char) ((0x20 | (v & 0x1f)) + 63));
            v >>= 5;
        }
        sb.append((char) (v + 63));
    }
}
