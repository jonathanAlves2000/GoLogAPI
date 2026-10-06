package GoLogAPI.service.routeOptimization;

public final class OptimizationDefaults {

    private OptimizationDefaults() {}

    public static final double DEFAULT_KM_COST_MULTIPLIER = 1.0;
    public static final double DEFAULT_HOUR_COST_MULTIPLIER = 1.0;
    public static final double DEFAULT_FIXED_COST_PER_VEHICLE = 0.0;
    public static final double DEFAULT_COST_PER_TRAVELED_HOUR = 0.0;
    public static final double DEFAULT_PENALTY_COST_UNSERVED = 100000.0;
    public static final double DEFAULT_LATE_ARRIVAL_COST_PER_HOUR = 0.0;

    public static final int DEFAULT_SERVICE_DURATION_SECONDS = 1800; // 30 minutos
    public static final int DEFAULT_TIME_WINDOW_LEAD_MINUTES = 15;
    public static final int DEFAULT_VEHICLE_START_WINDOW_LEAD_HOURS = 2;
    public static final int DEFAULT_VEHICLE_END_WINDOW_MARGIN_HOURS = 2;
    public static final int DEFAULT_GLOBAL_HORIZON_EXTRA_DAYS = 2;
}
