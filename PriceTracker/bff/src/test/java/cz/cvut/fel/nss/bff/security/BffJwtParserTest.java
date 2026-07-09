package cz.cvut.fel.nss.bff.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BffJwtParserTest {

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private BffJwtParser parser;

    @BeforeEach
    void setUp() {
        parser = new BffJwtParser();
        ReflectionTestUtils.setField(parser, "secret", SECRET);
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private String token(String subject, long expiryOffsetMs) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryOffsetMs))
                .signWith(key())
                .compact();
    }

    @Test
    void extractEmail_returnsSubject_forValidToken() {
        String header = "Bearer " + token("user@cvut.cz", 60_000);

        assertThat(parser.extractEmail(header)).contains("user@cvut.cz");
    }

    @Test
    void extractEmail_empty_whenHeaderNull() {
        assertThat(parser.extractEmail(null)).isEmpty();
    }

    @Test
    void extractEmail_empty_whenNoBearerPrefix() {
        assertThat(parser.extractEmail("Basic abc")).isEmpty();
    }

    @Test
    void extractEmail_empty_whenTokenExpired() {
        String header = "Bearer " + token("user@cvut.cz", -60_000);

        assertThat(parser.extractEmail(header)).isEmpty();
    }

    @Test
    void extractEmail_empty_whenSignatureInvalid() {
        String foreign = Jwts.builder()
                .subject("user@cvut.cz")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                        "AAAA635266556A586E3272357538782F413F4428472B4B6250645367566B5970")))
                .compact();

        assertThat(parser.extractEmail("Bearer " + foreign)).isEmpty();
    }
}
