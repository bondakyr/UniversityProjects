package cz.cvut.fel.nss.scraperservice.security;

import cz.cvut.fel.nss.commonshared.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalTokenValidator {

    @Value("${services.internal.token}")
    private String internalToken;

    public void requireValid(String providedToken) {
        if (providedToken == null || !providedToken.equals(internalToken)) {
            throw new BadRequestException("Invalid internal token");
        }
    }
}
