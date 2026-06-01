package GoLogAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeOperation {
        ENTREGA("ENTREGA"),
        COLETA("COLETA");
        private final String operation;
}

