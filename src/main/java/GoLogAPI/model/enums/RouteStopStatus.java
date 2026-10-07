package GoLogAPI.model.enums;

public enum RouteStopStatus {
    PENDENTE("Pendente"),
    EM_TRANSITO("Em Trânsito"),
    CHEGOU("Chegou no Raio (Geofence)"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído");

    private final String description;

    RouteStopStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
