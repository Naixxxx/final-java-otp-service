package dev.naixxxx.guardcode.domain;

import java.time.LocalDateTime;

public record OtpChallenge(
        long id,
        long userId,
        String operationRef,
        String codeValue,
        OtpState status,
        DeliveryChannel deliveryChannel,
        String destination,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime usedAt
) {}
