package dev.naixxxx.guardcode.api;

import com.sun.net.httpserver.HttpExchange;
import dev.naixxxx.guardcode.dto.OtpDtos;
import dev.naixxxx.guardcode.security.SessionUser;
import dev.naixxxx.guardcode.service.OtpFacade;
import dev.naixxxx.guardcode.util.HttpReplies;
import dev.naixxxx.guardcode.util.Json;
import dev.naixxxx.guardcode.util.Route;

public class OtpHandler extends ApiHandler {
    private final OtpFacade otp;
    public OtpHandler(OtpFacade otp) { this.otp = otp; }

    @Override protected void handleSafely(HttpExchange ex) throws Exception {
        SessionUser user = (SessionUser) ex.getAttribute("user");
        if (Route.is(ex, "POST", "/otp/generate")) {
            var req = Json.MAPPER.readValue(HttpReplies.body(ex), OtpDtos.GenerateRequest.class);
            HttpReplies.json(ex, 201, otp.generate(user, req));
            return;
        }
        if (Route.is(ex, "POST", "/otp/validate")) {
            var req = Json.MAPPER.readValue(HttpReplies.body(ex), OtpDtos.ValidateRequest.class);
            HttpReplies.json(ex, 200, otp.validate(user, req));
            return;
        }
        HttpReplies.error(ex, 404, "Unknown OTP endpoint");
    }
}
