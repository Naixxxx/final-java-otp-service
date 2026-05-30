package dev.naixxxx.guardcode.service;

import dev.naixxxx.guardcode.dao.OtpPolicyRepository;
import dev.naixxxx.guardcode.dao.OtpRepository;
import dev.naixxxx.guardcode.domain.OtpState;
import dev.naixxxx.guardcode.dto.OtpDtos;
import dev.naixxxx.guardcode.security.SessionUser;
import dev.naixxxx.guardcode.service.delivery.SenderRegistry;
import dev.naixxxx.guardcode.util.CodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OtpFacade {
    private static final Logger log = LoggerFactory.getLogger(OtpFacade.class);
    private final OtpPolicyRepository policyRepo;
    private final OtpRepository otpRepo;
    private final SenderRegistry senders;
    private final CodeFactory codeFactory;

    public OtpFacade(OtpPolicyRepository policyRepo, OtpRepository otpRepo, SenderRegistry senders, CodeFactory codeFactory) {
        this.policyRepo = policyRepo;
        this.otpRepo = otpRepo;
        this.senders = senders;
        this.codeFactory = codeFactory;
    }

    public OtpDtos.GenerateResponse generate(SessionUser user, OtpDtos.GenerateRequest req) {
        if (req.operationRef() == null || req.operationRef().isBlank()) throw new ServiceException(400, "operationRef is required");
        if (req.channel() == null) throw new ServiceException(400, "channel is required");
        if (req.destination() == null || req.destination().isBlank()) throw new ServiceException(400, "destination is required");
        var policy = policyRepo.get();
        String code = codeFactory.numeric(policy.codeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(policy.lifetimeSeconds());
        var saved = otpRepo.create(user.id(), req.operationRef().trim(), code, req.channel(), req.destination().trim(), expiresAt);
        senders.get(req.channel()).send(req.destination().trim(), code);
        log.info("OTP created: user={}, operation={}, channel={}, expiresAt={}", user.login(), req.operationRef(), req.channel(), expiresAt);
        return new OtpDtos.GenerateResponse(saved.id(), saved.operationRef(), saved.expiresAt());
    }

    public OtpDtos.ValidateResponse validate(SessionUser user, OtpDtos.ValidateRequest req) {
        if (req.operationRef() == null || req.operationRef().isBlank()) throw new ServiceException(400, "operationRef is required");
        if (req.code() == null || req.code().isBlank()) throw new ServiceException(400, "code is required");
        var found = otpRepo.findActive(user.id(), req.operationRef().trim(), req.code().trim());
        if (found.isEmpty()) return new OtpDtos.ValidateResponse(false, OtpState.EXPIRED);
        var otp = found.get();
        if (otp.expiresAt().isBefore(LocalDateTime.now())) {
            otpRepo.markExpired(otp.id());
            return new OtpDtos.ValidateResponse(false, OtpState.EXPIRED);
        }
        otpRepo.markUsed(otp.id());
        log.info("OTP used: user={}, operation={}", user.login(), req.operationRef());
        return new OtpDtos.ValidateResponse(true, OtpState.USED);
    }

    public int expireOverdue() { return otpRepo.expireOverdue(); }
}
