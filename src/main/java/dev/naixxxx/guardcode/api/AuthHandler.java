package dev.naixxxx.guardcode.api;

import com.sun.net.httpserver.HttpExchange;
import dev.naixxxx.guardcode.dto.AuthDtos;
import dev.naixxxx.guardcode.service.AuthFacade;
import dev.naixxxx.guardcode.util.HttpReplies;
import dev.naixxxx.guardcode.util.Json;
import dev.naixxxx.guardcode.util.Route;

public class AuthHandler extends ApiHandler {
    private final AuthFacade auth;
    public AuthHandler(AuthFacade auth) { this.auth = auth; }

    @Override protected void handleSafely(HttpExchange ex) throws Exception {
        if (Route.is(ex, "POST", "/auth/register")) {
            var req = Json.MAPPER.readValue(HttpReplies.body(ex), AuthDtos.RegisterRequest.class);
            auth.register(req);
            HttpReplies.json(ex, 201, java.util.Map.of("status", "registered"));
            return;
        }
        if (Route.is(ex, "POST", "/auth/login")) {
            var req = Json.MAPPER.readValue(HttpReplies.body(ex), AuthDtos.LoginRequest.class);
            HttpReplies.json(ex, 200, auth.login(req));
            return;
        }
        HttpReplies.error(ex, 404, "Unknown auth endpoint");
    }
}
