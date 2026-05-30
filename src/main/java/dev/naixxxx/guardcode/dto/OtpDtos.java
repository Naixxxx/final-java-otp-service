package dev.naixxxx.guardcode.dto;

import dev.naixxxx.guardcode.domain.DeliveryChannel;
import dev.naixxxx.guardcode.domain.OtpState;
import java.time.LocalDateTime;

public final class OtpDtos {
    private OtpDtos() {}
    public record GenerateRequest(String operationRef, DeliveryChannel channel, String destination) {}
    public record GenerateResponse(long otpId, String operationRef, LocalDateTime expiresAt) {}
    public record ValidateRequest(String operationRef, String code) {}
    public record ValidateResponse(boolean valid, OtpState status) {}
}
