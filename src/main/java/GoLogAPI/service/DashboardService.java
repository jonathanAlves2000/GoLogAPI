package GoLogAPI.service;

import GoLogAPI.dto.dashboard.DashboardMetricsResponse;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DriverRepository driverRepository;
    private final TransportRepository transportRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteStopRepository routeStopRepository;

    public DashboardService(DriverRepository driverRepository,
                            TransportRepository transportRepository,
                            ShipmentRepository shipmentRepository,
                            RouteStopRepository routeStopRepository) {
        this.driverRepository = driverRepository;
        this.transportRepository = transportRepository;
        this.shipmentRepository = shipmentRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public DashboardMetricsResponse getMetrics() {
        List<Driver> drivers = driverRepository.findAll();
        List<Transport> transports = transportRepository.findAll();
        List<Shipment> shipments = shipmentRepository.findAll();
        List<RouteStop> routeStops = routeStopRepository.findAll();

        Map<UUID, RouteStop> shipmentToRouteStop = routeStops.stream()
                .filter(rs -> rs.getShipment() != null)
                .collect(Collectors.toMap(rs -> rs.getShipment().getId(), rs -> rs, (rs1, rs2) -> rs1));

        long quantidadeMotoristas = drivers.size();

        long rotasEmAndamento = transports.stream()
                .filter(t -> t.getDriver() != null && (t.getRouteCompleted() == null || t.getRouteCompleted().isBlank()))
                .count();

        long rotasConcluidas = transports.stream()
                .filter(t -> t.getRouteCompleted() != null && !t.getRouteCompleted().isBlank())
                .count();

        long totalTransports = transports.size();
        long transportsWithDriver = transports.stream().filter(t -> t.getDriver() != null).count();
        double taxaAlocacao = totalTransports == 0 ? 0.0 : ((double) transportsWithDriver / totalTransports) * 100.0;
        taxaAlocacao = Math.round(taxaAlocacao * 100.0) / 100.0;

        long entregasEmTransporte = shipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA)
                .filter(s -> {
                    RouteStop rs = shipmentToRouteStop.get(s.getId());
                    if (rs == null) return false;
                    Transport t = rs.getTransport();
                    return t != null && t.getDriver() != null && (t.getRouteCompleted() == null || t.getRouteCompleted().isBlank())
                            && (rs.getRouteCompleted() == null || rs.getRouteCompleted().isBlank());
                })
                .count();

        long backlogEntregasPendentes = shipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA)
                .filter(s -> !isShipmentCompleted(s, shipmentToRouteStop))
                .count();

        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now();

        List<Shipment> todayShipments = shipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA)
                .filter(s -> s.getSchedulind() != null && s.getSchedulind().toLocalDate().equals(today))
                .toList();

        long onTimeCount = 0;
        for (Shipment s : todayShipments) {
            boolean completed = isShipmentCompleted(s, shipmentToRouteStop);
            if (completed) {
                LocalDateTime completionTime = LocalDateTime.ofInstant(s.getUpdatedAt(), zone);
                if (!completionTime.isAfter(s.getSchedulind())) {
                    onTimeCount++;
                }
            } else {
                if (!now.isAfter(s.getSchedulind())) {
                    onTimeCount++;
                }
            }
        }

        double slaDia = todayShipments.isEmpty() ? 100.0 : ((double) onTimeCount / todayShipments.size()) * 100.0;
        slaDia = Math.round(slaDia * 100.0) / 100.0;

        long atrasos = shipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA)
                .filter(s -> {
                    boolean completed = isShipmentCompleted(s, shipmentToRouteStop);
                    if (completed) {
                        LocalDateTime completionTime = LocalDateTime.ofInstant(s.getUpdatedAt(), zone);
                        return completionTime.isAfter(s.getSchedulind());
                    } else {
                        return now.isAfter(s.getSchedulind());
                    }
                })
                .count();

        double custoTotalPlanejado = transports.stream()
                .mapToDouble(t -> t.getTotalCostCalculed() != null ? t.getTotalCostCalculed() : 0.0)
                .sum();
        custoTotalPlanejado = Math.round(custoTotalPlanejado * 100.0) / 100.0;

        double custoTotalEfetivo = transports.stream()
                .mapToDouble(t -> t.getTotalCost() != null ? t.getTotalCost() : 0.0)
                .sum();
        custoTotalEfetivo = Math.round(custoTotalEfetivo * 100.0) / 100.0;

        double emissaoCo2Planejada = transports.stream()
                .mapToDouble(t -> {
                    double co2PerKm = getTransportCo2PerKm(t);
                    double distance = t.getCalculedDistance() != null ? t.getCalculedDistance() : 0.0;
                    return (distance / 1000.0) * co2PerKm;
                })
                .sum();
        emissaoCo2Planejada = Math.round(emissaoCo2Planejada * 100.0) / 100.0;

        double emissaoCo2Efetiva = transports.stream()
                .mapToDouble(t -> {
                    double co2PerKm = getTransportCo2PerKm(t);
                    double distance = t.getDistanceTraveled() != null ? t.getDistanceTraveled() : 0.0;
                    return (distance / 1000.0) * co2PerKm;
                })
                .sum();
        emissaoCo2Efetiva = Math.round(emissaoCo2Efetiva * 100.0) / 100.0;

        return new DashboardMetricsResponse(
                quantidadeMotoristas,
                rotasEmAndamento,
                entregasEmTransporte,
                backlogEntregasPendentes,
                taxaAlocacao,
                rotasConcluidas,
                slaDia,
                atrasos,
                custoTotalPlanejado,
                custoTotalEfetivo,
                emissaoCo2Planejada,
                emissaoCo2Efetiva
        );
    }

    private double getTransportCo2PerKm(Transport transport) {
        EquipamentGroup group = transport.getEquipamentGroup();
        if (group == null) {
            return 0.0;
        }
        if (group.getEquipament1() instanceof Tractor) {
            Double co2 = ((Tractor) group.getEquipament1()).getCo2PerKm();
            return co2 != null ? co2 : 0.0;
        }
        if (group.getEquipament2() instanceof Tractor) {
            Double co2 = ((Tractor) group.getEquipament2()).getCo2PerKm();
            return co2 != null ? co2 : 0.0;
        }
        if (group.getEquipament3() instanceof Tractor) {
            Double co2 = ((Tractor) group.getEquipament3()).getCo2PerKm();
            return co2 != null ? co2 : 0.0;
        }
        return 0.0;
    }

    private boolean isShipmentCompleted(Shipment shipment, Map<UUID, RouteStop> shipmentToRouteStop) {
        RouteStop rs = shipmentToRouteStop.get(shipment.getId());
        if (rs == null) {
            return false;
        }
        if (rs.getRouteCompleted() != null && !rs.getRouteCompleted().isBlank()) {
            return true;
        }
        if (rs.getTransport() != null && rs.getTransport().getRouteCompleted() != null && !rs.getTransport().getRouteCompleted().isBlank()) {
            return true;
        }
        return false;
    }
}
