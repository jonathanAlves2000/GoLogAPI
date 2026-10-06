package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.request.LoadDemand;
import GoLogAPI.dto.dtoRouteOptimization.request.LoadLimit;
import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.repository.EquipamentCapacityRepository;
import GoLogAPI.repository.OptimizationProfileRepository;
import GoLogAPI.repository.ShipmentDemandRepository;
import GoLogAPI.repository.TrailerRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OptimizationConfigResolver {

    private final OptimizationProfileRepository optimizationProfileRepository;
    private final EquipamentCapacityRepository equipamentCapacityRepository;
    private final ShipmentDemandRepository shipmentDemandRepository;
    private final TrailerRepository trailerRepository;

    public OptimizationConfigResolver(
            OptimizationProfileRepository optimizationProfileRepository,
            EquipamentCapacityRepository equipamentCapacityRepository,
            ShipmentDemandRepository shipmentDemandRepository,
            TrailerRepository trailerRepository) {
        this.optimizationProfileRepository = optimizationProfileRepository;
        this.equipamentCapacityRepository = equipamentCapacityRepository;
        this.shipmentDemandRepository = shipmentDemandRepository;
        this.trailerRepository = trailerRepository;
    }

    public ResolvedOptimizationSettings resolveSettings(OptimizeRouteRequest request, Company company) {
        OptimizationProfile profile = null;

        if (request.profileId() != null) {
            profile = optimizationProfileRepository.findById(request.profileId()).orElse(null);
        } else if (company != null && company.getId() != null) {
            profile = optimizationProfileRepository.findFirstByCompanyIdAndIsDefaultTrue(company.getId())
                    .or(() -> optimizationProfileRepository.findFirstByCompanyId(company.getId()))
                    .orElse(null);
        }

        // 1. kmCostMultiplier
        double kmMultiplier;
        if (request.kmCostMultiplier() != null) {
            kmMultiplier = request.kmCostMultiplier();
        } else if (profile != null && profile.getKmCostMultiplier() != null) {
            kmMultiplier = profile.getKmCostMultiplier();
        } else if (request.routePriority() != null) {
            kmMultiplier = request.routePriority() == RoutePriority.TEMPO ? 0.1 : 1.0;
        } else {
            kmMultiplier = OptimizationDefaults.DEFAULT_KM_COST_MULTIPLIER;
        }

        // 2. hourCostMultiplier
        double hourMultiplier;
        if (request.hourCostMultiplier() != null) {
            hourMultiplier = request.hourCostMultiplier();
        } else if (profile != null && profile.getHourCostMultiplier() != null) {
            hourMultiplier = profile.getHourCostMultiplier();
        } else if (request.routePriority() != null) {
            hourMultiplier = request.routePriority() == RoutePriority.TEMPO ? 2.0 : 1.0;
        } else {
            hourMultiplier = OptimizationDefaults.DEFAULT_HOUR_COST_MULTIPLIER;
        }

        // 3. fixedCostPerVehicle
        double fixedCost = request.fixedCostPerVehicle() != null ? request.fixedCostPerVehicle()
                : (profile != null && profile.getFixedCostPerVehicle() != null ? profile.getFixedCostPerVehicle()
                : OptimizationDefaults.DEFAULT_FIXED_COST_PER_VEHICLE);

        // 4. costPerTraveledHour
        double costPerTraveledHour = (profile != null && profile.getCostPerTraveledHour() != null)
                ? profile.getCostPerTraveledHour()
                : OptimizationDefaults.DEFAULT_COST_PER_TRAVELED_HOUR;

        // 5. penaltyCostUnserved
        double penaltyCost = request.penaltyCostUnserved() != null ? request.penaltyCostUnserved()
                : (profile != null && profile.getPenaltyCostUnserved() != null ? profile.getPenaltyCostUnserved()
                : OptimizationDefaults.DEFAULT_PENALTY_COST_UNSERVED);

        // 6. defaultServiceDurationSeconds
        int serviceDuration = request.defaultServiceDurationSeconds() != null ? request.defaultServiceDurationSeconds()
                : (profile != null && profile.getDefaultServiceDurationSeconds() != null ? profile.getDefaultServiceDurationSeconds()
                : OptimizationDefaults.DEFAULT_SERVICE_DURATION_SECONDS);

        // 7. timeWindowLeadMinutes
        int timeWindowLeadMinutes = request.timeWindowLeadMinutes() != null ? request.timeWindowLeadMinutes()
                : (profile != null && profile.getTimeWindowLeadMinutes() != null ? profile.getTimeWindowLeadMinutes()
                : OptimizationDefaults.DEFAULT_TIME_WINDOW_LEAD_MINUTES);

        // 8. vehicleStartWindowLeadHours
        int vehicleStartWindowLeadHours = (profile != null && profile.getVehicleStartWindowLeadHours() != null)
                ? profile.getVehicleStartWindowLeadHours()
                : OptimizationDefaults.DEFAULT_VEHICLE_START_WINDOW_LEAD_HOURS;

        // 9. vehicleEndWindowMarginHours
        int vehicleEndWindowMarginHours = (profile != null && profile.getVehicleEndWindowMarginHours() != null)
                ? profile.getVehicleEndWindowMarginHours()
                : OptimizationDefaults.DEFAULT_VEHICLE_END_WINDOW_MARGIN_HOURS;

        // 10. globalHorizonExtraDays
        int globalHorizonExtraDays = (profile != null && profile.getGlobalHorizonExtraDays() != null)
                ? profile.getGlobalHorizonExtraDays()
                : OptimizationDefaults.DEFAULT_GLOBAL_HORIZON_EXTRA_DAYS;

        return new ResolvedOptimizationSettings(
                kmMultiplier,
                hourMultiplier,
                fixedCost,
                costPerTraveledHour,
                penaltyCost,
                serviceDuration,
                timeWindowLeadMinutes,
                vehicleStartWindowLeadHours,
                vehicleEndWindowMarginHours,
                globalHorizonExtraDays
        );
    }

    /**
     * Resolve capacidades do conjunto de equipamentos com fallback para peso e volume legados.
     */
    public Map<String, LoadLimit> resolveVehicleLoadLimits(EquipamentGroup equipamentGroup) {
        Map<String, LoadLimit> limits = new HashMap<>();

        // 1. Busca capacidades cadastradas para o trator (equipament1)
        if (equipamentGroup.getEquipament1() != null) {
            List<EquipamentCapacity> capacities1 = equipamentCapacityRepository.findByEquipamentId(equipamentGroup.getEquipament1().getId());
            for (EquipamentCapacity cap : capacities1) {
                limits.put(cap.getDemandType().getCode(), new LoadLimit(
                        String.valueOf(cap.getMaxCapacity().longValue()),
                        cap.getSoftMaxCapacity() != null ? String.valueOf(cap.getSoftMaxCapacity().longValue()) : null,
                        cap.getCostPerUnitAboveSoftMax()
                ));
            }
        }

        // 2. Busca capacidades cadastradas para carretas acopladas (equipament2 e equipament3)
        addTrailerCapacities(equipamentGroup.getEquipament2(), limits);
        addTrailerCapacities(equipamentGroup.getEquipament3(), limits);

        // 3. Fallback para PESO (weight): se não configurado dinamicamente, usa maximumCapacity do trator
        if (!limits.containsKey("weight") && equipamentGroup.getEquipament1() != null && equipamentGroup.getEquipament1().getMaximumCapacity() != null) {
            limits.put("weight", new LoadLimit(String.valueOf(equipamentGroup.getEquipament1().getMaximumCapacity().longValue())));
        }

        // 4. Fallback para VOLUME (volume): se não configurado dinamicamente, busca maximumVolume do trailer
        if (!limits.containsKey("volume") && equipamentGroup.getEquipament2() != null) {
            trailerRepository.findById(equipamentGroup.getEquipament2().getId()).ifPresent(trailer -> {
                if (trailer.getMaximumVolume() != null) {
                    limits.put("volume", new LoadLimit(String.valueOf(trailer.getMaximumVolume().longValue())));
                }
            });
        }

        return limits;
    }

    private void addTrailerCapacities(Equipament equipament, Map<String, LoadLimit> limits) {
        if (equipament != null) {
            List<EquipamentCapacity> capacities = equipamentCapacityRepository.findByEquipamentId(equipament.getId());
            for (EquipamentCapacity cap : capacities) {
                String code = cap.getDemandType().getCode();
                long max = cap.getMaxCapacity().longValue();
                if (limits.containsKey(code)) {
                    // Soma a capacidade dos semi-reboques
                    long previousMax = Long.parseLong(limits.get(code).maxLoad());
                    limits.put(code, new LoadLimit(String.valueOf(previousMax + max)));
                } else {
                    limits.put(code, new LoadLimit(String.valueOf(max)));
                }
            }
        }
    }

    /**
     * Resolve demandas de uma carga com fallback para peso e volume legados.
     */
    public Map<String, LoadDemand> resolveShipmentLoadDemands(Shipment shipment) {
        Map<String, LoadDemand> demands = new HashMap<>();

        // 1. Busca demandas cadastradas dinamicamente
        List<ShipmentDemand> dynamicDemands = shipmentDemandRepository.findByShipmentId(shipment.getId());
        for (ShipmentDemand dem : dynamicDemands) {
            demands.put(dem.getDemandType().getCode(), new LoadDemand(String.valueOf(Math.abs(dem.getAmount().longValue()))));
        }

        // 2. Fallback para PESO (weight) se não configurado
        if (!demands.containsKey("weight") && shipment.getWeight() != null) {
            demands.put("weight", new LoadDemand(String.valueOf(Math.abs(shipment.getWeight().longValue()))));
        }

        // 3. Fallback para VOLUME (volume) se não configurado
        if (!demands.containsKey("volume") && shipment.getVolume() != null && shipment.getVolume() > 0) {
            demands.put("volume", new LoadDemand(String.valueOf(Math.abs(shipment.getVolume().longValue()))));
        }

        return demands;
    }

    public record ResolvedOptimizationSettings(
            double kmCostMultiplier,
            double hourCostMultiplier,
            double fixedCostPerVehicle,
            double costPerTraveledHour,
            double penaltyCostUnserved,
            int defaultServiceDurationSeconds,
            int timeWindowLeadMinutes,
            int vehicleStartWindowLeadHours,
            int vehicleEndWindowMarginHours,
            int globalHorizonExtraDays
    ) {}
}
