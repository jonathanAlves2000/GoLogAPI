package GoLogAPI.infra.tenant;

import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<UUID> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> userCompanyId = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isMaster = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<String> companyType = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static void setCurrentTenantId(UUID tenantId) {
        currentTenantId.set(tenantId);
    }

    public static UUID getUserCompanyId() {
        return userCompanyId.get();
    }

    public static void setUserCompanyId(UUID companyId) {
        userCompanyId.set(companyId);
    }

    public static boolean isMaster() {
        Boolean master = isMaster.get();
        return master != null && master;
    }

    public static void setMaster(boolean master) {
        isMaster.set(master);
    }

    public static String getCompanyType() {
        return companyType.get();
    }

    public static void setCompanyType(String type) {
        companyType.set(type);
    }

    public static void clear() {
        currentTenantId.remove();
        userCompanyId.remove();
        isMaster.remove();
        companyType.remove();
    }
}
