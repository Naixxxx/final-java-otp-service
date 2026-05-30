package dev.naixxxx.guardcode.security;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import dev.naixxxx.guardcode.domain.UserRole;
import dev.naixxxx.guardcode.util.HttpReplies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthFilter extends Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private final JwtService jwt;
    private final UserRole requiredRole;

    public AuthFilter(JwtService jwt, UserRole requiredRole) {
        this.jwt = jwt;
        this.requiredRole = requiredRole;
    }

    @Override public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            String header = exchange.getRequestHeaders().getFirst("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                HttpReplies.error(exchange, 401, "Bearer token is required");
                return;
            }
            SessionUser user = jwt.verify(header.substring("Bearer ".length()).trim());
            if (requiredRole != null && user.role() != requiredRole) {
                HttpReplies.error(exchange, 403, "Forbidden for role " + user.role());
                return;
            }
            exchange.setAttribute("user", user);
            chain.doFilter(exchange);
        } catch (Exception e) {
            log.warn("Authentication failed: {}", e.getMessage());
            HttpReplies.error(exchange, 401, "Invalid or expired token");
        }
    }

    @Override public String description() { return "JWT role filter"; }
}
