package cz.cvut.fel.nss.bff.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticatedUser {
    private Long id;
    private String email;
    private String login;
    private String role;
}
