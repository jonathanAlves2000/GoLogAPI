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

    public RouteStopService(RouteStopRepository routeStopRepository){
        this.routeStopRepository = routeStopRepository;
    }

    public List<RouteStopResponseList> getAll(){
        List<RouteStop> routeStopResponses = routeStopRepository.findAll();
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
                        routeStop.getShipment()
                )).toList();
    }
}
