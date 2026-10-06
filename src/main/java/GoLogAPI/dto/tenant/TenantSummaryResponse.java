package GoLogAPI.dto.tenant;

public record TenantSummaryResponse(
        long totalTenants,
        long totalBranches,
        long totalUsers,
        long totalVehicles,
        long totalShipments
) {}
