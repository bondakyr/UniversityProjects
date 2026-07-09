package cz.cvut.fel.nss.userservice.config;

import cz.cvut.fel.nss.userservice.entity.User;
import cz.cvut.fel.nss.userservice.enums.UserRole;
import cz.cvut.fel.nss.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-enabled:true}")
    private boolean enabled;

    @Value("${app.admin.email:admin@pricetracker.local}")
    private String adminEmail;

    @Value("${app.admin.login:admin}")
    private String adminLogin;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!enabled) {
            log.info("Admin bootstrap disabled.");
            return;
        }

        User admin = userRepository.findByEmail(adminEmail)
                .or(() -> userRepository.findByLogin(adminLogin))
                .orElseGet(User::new);

        admin.setEmail(adminEmail);
        admin.setLogin(adminLogin);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        log.info("Admin account is ready: email={}, login={}", adminEmail, adminLogin);
    }
}
