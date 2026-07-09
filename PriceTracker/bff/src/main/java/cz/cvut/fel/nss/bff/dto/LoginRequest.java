package cz.cvut.fel.nss.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Login body. Exists so Swagger renders a form; user-service does the real validation. */
@Data
@Schema(name = "LoginRequest", description = "Credentials for logging in")
public class LoginRequest {
    @Schema(example = "watcher@cvut.cz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(example = "watch_me_now", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
