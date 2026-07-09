package cz.cvut.fel.nss.bff.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StripUpstreamHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, new HopByHopStrippingResponse(response));
    }

    private static boolean isHopByHop(String name) {
        return "Transfer-Encoding".equalsIgnoreCase(name)
                || "Content-Length".equalsIgnoreCase(name)
                || "Connection".equalsIgnoreCase(name);
    }

    private static final class HopByHopStrippingResponse extends HttpServletResponseWrapper {
        HopByHopStrippingResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            if (!isHopByHop(name)) super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (!isHopByHop(name)) super.addHeader(name, value);
        }
    }
}
