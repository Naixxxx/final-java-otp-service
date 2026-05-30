package dev.naixxxx.guardcode.api;

import com.sun.net.httpserver.HttpExchange;
import dev.naixxxx.guardcode.dto.AdminDtos;
import dev.naixxxx.guardcode.service.AdminFacade;
import dev.naixxxx.guardcode.util.HttpReplies;
import dev.naixxxx.guardcode.util.Json;
import dev.naixxxx.guardcode.util.Route;

public class AdminHandler extends ApiHandler {
    private final AdminFacade admin;
    public AdminHandler(AdminFacade admin) { this.admin = admin; }

    @Override protected void handleSafely(HttpExchange ex) throws Exception {
        if (Route.is(ex, "GET", "/admin/otp-policy")) {
            HttpReplies.json(ex, 200, admin.getPolicy());
            return;
        }
        if (Route.is(ex, "PUT", "/admin/otp-policy")) {
            var req = Json.MAPPER.readValue(HttpReplies.body(ex), AdminDtos.PolicyRequest.class);
            HttpReplies.json(ex, 200, admin.updatePolicy(req));
            return;
        }
        if (Route.is(ex, "GET", "/admin/users")) {
            HttpReplies.json(ex, 200, admin.users());
            return;
        }
        if (Route.starts(ex, "DELETE", "/admin/users/")) {
            String id = ex.getRequestURI().getPath().substring("/admin/users/".length());
            admin.deleteUser(Long.parseLong(id));
            HttpReplies.empty(ex, 204);
            return;
        }
        HttpReplies.error(ex, 404, "Unknown admin endpoint");
    }
}
