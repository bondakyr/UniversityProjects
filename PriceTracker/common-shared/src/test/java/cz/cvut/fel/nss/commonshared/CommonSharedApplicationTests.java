package cz.cvut.fel.nss.commonshared;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootTest(properties = {
        "jwt.secret=QkFTRTY0U0VDUkVUS0VZMDEyMzQ1Njc4OTAxMjM0NTY3OA==",
        "jwt.expiration-ms=3600000"
})
class CommonSharedApplicationTests {

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void contextLoads() {
    }

}
