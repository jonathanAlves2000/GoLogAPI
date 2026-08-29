package GoLogAPI.model.enums;

public enum ShipmentStatus {
    PENDENTE("Pendente"),
    AGUARDANDO_INICIO("Aguardando Inicio"),
    INICIADO("Iniciado"),
    FINALIZADO("Finalizado");

    private final String status;

    ShipmentStatus(String status) {
        this.status = status;
    }

    public String getStatus(String status){
        return this.status;
    }
}
