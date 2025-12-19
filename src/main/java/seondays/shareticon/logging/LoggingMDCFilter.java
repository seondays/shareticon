package seondays.shareticon.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import seondays.shareticon.login.CustomOAuth2User;

@Component
public class LoggingMDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User principal) {
                MDC.put("userId", principal.getId().toString());
            } else {
                MDC.put("userId", "anonymousUser");
            }

            String requestID = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("requestId", requestID);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
