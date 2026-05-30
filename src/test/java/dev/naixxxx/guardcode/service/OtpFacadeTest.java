package dev.naixxxx.guardcode.service;

import dev.naixxxx.guardcode.dao.OtpPolicyRepository;
import dev.naixxxx.guardcode.dao.OtpRepository;
import dev.naixxxx.guardcode.domain.*;
import dev.naixxxx.guardcode.domain.*;
import dev.naixxxx.guardcode.dto.OtpDtos;
import dev.naixxxx.guardcode.security.SessionUser;
import dev.naixxxx.guardcode.service.delivery.CodeSender;
import dev.naixxxx.guardcode.service.delivery.SenderRegistry;
import dev.naixxxx.guardcode.util.CodeFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OtpFacadeTest {
    @Test
    void generateSavesCodeAndCallsSender() {
        OtpPolicyRepository policies = mock(OtpPolicyRepository.class);
        OtpRepository otps = mock(OtpRepository.class);
        CodeSender sender = mock(CodeSender.class);
        when(sender.channel()).thenReturn(DeliveryChannel.FILE);
        when(policies.get()).thenReturn(new OtpPolicy(6, 300));
        when(otps.create(anyLong(), eq("OP-1"), anyString(), eq(DeliveryChannel.FILE), eq("target"), any()))
                .thenAnswer(inv -> new OtpChallenge(10, inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        OtpState.ACTIVE, inv.getArgument(3), inv.getArgument(4), LocalDateTime.now(), inv.getArgument(5), null));
        OtpFacade facade = new OtpFacade(policies, otps, new SenderRegistry(List.of(sender)), new CodeFactory());
        var response = facade.generate(new SessionUser(5, "u", UserRole.USER), new OtpDtos.GenerateRequest("OP-1", DeliveryChannel.FILE, "target"));
        assertEquals(10, response.otpId());
        verify(sender).send(eq("target"), matches("\\d{6}"));
    }

    @Test
    void validateMarksActiveCodeAsUsed() {
        OtpPolicyRepository policies = mock(OtpPolicyRepository.class);
        OtpRepository otps = mock(OtpRepository.class);
        when(otps.findActive(5, "OP-1", "123456")).thenReturn(Optional.of(new OtpChallenge(
                10, 5, "OP-1", "123456", OtpState.ACTIVE, DeliveryChannel.FILE,
                "target", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), null)));
        OtpFacade facade = new OtpFacade(policies, otps, new SenderRegistry(List.of()), new CodeFactory());
        var result = facade.validate(new SessionUser(5, "u", UserRole.USER), new OtpDtos.ValidateRequest("OP-1", "123456"));
        assertTrue(result.valid());
        assertEquals(OtpState.USED, result.status());
        verify(otps).markUsed(10);
    }
}
