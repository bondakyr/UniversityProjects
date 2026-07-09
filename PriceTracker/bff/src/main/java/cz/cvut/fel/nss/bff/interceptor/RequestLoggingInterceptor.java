package cz.cvut.fel.nss.bff.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        log.info("--> {} {}{}", request.getMethod(), request.getRequestURI(), query);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object start = request.getAttribute(START_TIME);
        long tookMs = (start instanceof Long) ? System.currentTimeMillis() - (Long) start : -1;
        log.info("<-- {} {} [{}] {} ms", request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs);
    }
}
