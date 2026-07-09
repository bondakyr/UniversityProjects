package cz.cvut.fel.nss.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {"cz.cvut.fel.nss.bff", "cz.cvut.fel.nss.commonshared.exception", "cz.cvut.fel.nss.commonshared.payload"},
        exclude = { UserDetailsServiceAutoConfiguration.class }
)
public class BffApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
