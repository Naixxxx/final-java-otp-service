package dev.naixxxx.guardcode.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.naixxxx.guardcode.service.ServiceException;
import dev.naixxxx.guardcode.util.HttpReplies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ApiHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiHandler.class);

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        long started = System.currentTimeMillis();
        try {
            log.info("{} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath());
            handleSafely(exchange);
        } catch (ServiceException e) {
            log.warn("Request failed with service error: {}", e.getMessage());
            HttpReplies.error(exchange, e.httpStatus(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Bad request: {}", e.getMessage());
            HttpReplies.error(exchange, 400, e.getMessage());
        } catch (Exception e) {
            log.error("Unhandled API error", e);
            HttpReplies.error(exchange, 500, "Internal server error");
        } finally {
            log.info("{} {} finished in {} ms", exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    System.currentTimeMillis() - started);
        }
    }

    protected abstract void handleSafely(HttpExchange exchange) throws Exception;
}
