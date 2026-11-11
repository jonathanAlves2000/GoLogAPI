package GoLogAPI.service;

import GoLogAPI.dto.routeStop.RouteStopResponseList;
import GoLogAPI.model.RouteStop;
import GoLogAPI.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RouteStopService {

    private final RouteStopRepository routeStopRepository;

    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public RouteStopService(RouteStopRepository routeStopRepository, GoLogAPI.infra.tenant.TenantGuard tenantGuard){
        this.routeStopRepository = routeStopRepository;
        this.tenantGuard = tenantGuard;
    }

    public List<RouteStopResponseList> getAll(){
        boolean isDriver = "DRIVER".equals(GoLogAPI.infra.tenant.TenantContext.getCurrentRole());
        java.util.UUID me = GoLogAPI.infra.tenant.TenantContext.getCurrentUserId();
        List<RouteStop> routeStopResponses = routeStopRepository.findAll().stream()
                .filter(rs -> rs.getTransport() != null)
                .filter(rs -> isDriver
                        ? rs.getTransport().getDriver() != null && rs.getTransport().getDriver().getUser() != null
                            && rs.getTransport().getDriver().getUser().getId().equals(me)
                        : tenantGuard.canSee(rs.getTransport().getTransporter()))
                .toList();
        return routeStopResponses.stream()
                .map(routeStop -> new RouteStopResponseList(
                        routeStop.getId(),
                        routeStop.getSequenceOrder(),
                        routeStop.getRoutePlanned(),
                        routeStop.getRouteCompleted(),
                        routeStop.getCalculatedCost(),
                        routeStop.getRealizedCots(),
                        routeStop.getCalculatedDistance(),
                        routeStop.getRealizedDistance(),
                        routeStop.getCalculatedDuration(),
                        routeStop.getRealizedDuration(),
                        routeStop.getCalculatedWait(),
                        routeStop.getRealizedWait(),
                        routeStop.getTransport(),
                        routeStop.getShipment(),
                        routeStop.getStatus(),
                        routeStop.getArrivedAt(),
                        routeStop.getDepartedAt(),
                        routeStop.getAutoCheckIn(),
                        routeStop.getCheckInDistance()
                )).toList();
    }
}
