package GoLogAPI.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompanyType {
    TENANT_MASTER("GoLog Master"),
    TENANT_HEADQUARTER("Empresa Matriz"),
    TENANT_BRANCH("Empresa Filial"),
    CLIENT("Cliente / Destinatário"),
    SUPPLIER("Fornecedor / Parceiro");

    private final String description;
}
