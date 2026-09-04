package GoLogAPI.model.enums;

public enum WorkScheduleStatus {
    ATIVO("Escala Ativa"),
    DESATIVADO("Escala Desativada");

    private final String status;

    WorkScheduleStatus(String status){
        this.status = status;
    }

    public String getStatus(String status){
        return status;
    }
}

