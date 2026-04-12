package GoLogAPI.dto.exeption;

import java.util.List;

public record ConflictReponse(
        List<String> message) {
}
