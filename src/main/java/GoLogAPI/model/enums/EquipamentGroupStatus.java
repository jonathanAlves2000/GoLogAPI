package GoLogAPI.model.enums;

public enum EquipamentGroupStatus {
    DISPONIVEL("Equipamento Ativo"),
    INDISPONIVEL("Equipamento Inativo");

    private final String status;

    EquipamentGroupStatus(String status){
        this.status = status;
    }

    public String getStatus(String status){
        return status;
    }
}
