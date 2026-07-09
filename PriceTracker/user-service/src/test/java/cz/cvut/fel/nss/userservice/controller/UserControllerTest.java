package cz.cvut.fel.nss.userservice.controller;

import cz.cvut.fel.nss.commonshared.exception.ResourceNotFoundException;
import cz.cvut.fel.nss.userservice.dto.user.UserResponse;
import cz.cvut.fel.nss.userservice.entity.User;
import cz.cvut.fel.nss.userservice.enums.UserRole;
import cz.cvut.fel.nss.userservice.repository.UserRepository;
import cz.cvut.fel.nss.userservice.security.InternalTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String INTERNAL_TOKEN = "pt-internal-secret-2026";

    @Mock
    private UserRepository userRepository;

    @Mock
    private InternalTokenValidator internalTokenValidator;

    @InjectMocks
    private UserController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);
        user.setEmail("user@cvut.cz");
        user.setLogin("user7");
        user.setRole(UserRole.USER);
    }

    @Test
    void getById_returnsUser() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        ResponseEntity<UserResponse> resp = controller.getById(INTERNAL_TOKEN, 7L);

        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isEqualTo(7L);
        assertThat(resp.getBody().getEmail()).isEqualTo("user@cvut.cz");
        assertThat(resp.getBody().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void getByEmail_returnsUser() {
        when(userRepository.findByEmail("user@cvut.cz")).thenReturn(Optional.of(user));

        ResponseEntity<UserResponse> resp = controller.getByEmail(INTERNAL_TOKEN, "user@cvut.cz");

        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getLogin()).isEqualTo("user7");
    }

    @Test
    void getById_throws_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getById(INTERNAL_TOKEN, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
