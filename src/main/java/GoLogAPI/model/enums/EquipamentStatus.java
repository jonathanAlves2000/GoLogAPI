package GoLogAPI.model.enums;

public enum EquipamentStatus {

    ATIVO("Equipamento Ativo"),
    DESATIVADO("Equipamento Desativado"),
    EM_MANUTENCAO("Equipamento em Manutenção");

    private final String status;

    EquipamentStatus(String status){
        this.status = status;
    }

    public String getStatus(String status){
        return status;
    }

}
