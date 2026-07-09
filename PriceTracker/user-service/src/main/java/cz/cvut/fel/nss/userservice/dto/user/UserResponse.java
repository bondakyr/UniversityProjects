package cz.cvut.fel.nss.userservice.dto.user;

import cz.cvut.fel.nss.userservice.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String login;
    private String firstName;
    private String lastName;
    private UserRole role;
}
