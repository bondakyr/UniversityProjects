package cz.cvut.fel.nss.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Register body; mirrors user-service field-for-field so Jackson serialises the exact JSON it expects. */
@Data
@Schema(name = "RegisterRequest", description = "New account details")
public class RegisterRequest {
    @Schema(example = "watcher@cvut.cz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(example = "watcher", requiredMode = Schema.RequiredMode.REQUIRED, description = "3-20 characters")
    private String login;

    @Schema(example = "watch_me_now", requiredMode = Schema.RequiredMode.REQUIRED, description = "at least 6 characters")
    private String password;
}