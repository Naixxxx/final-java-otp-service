package dev.naixxxx.guardcode.dto;

import dev.naixxxx.guardcode.domain.UserRole;
import java.time.LocalDateTime;

public final class AdminDtos {
    private AdminDtos() {}
    public record PolicyRequest(int codeLength, int lifetimeSeconds) {}
    public record PolicyResponse(int codeLength, int lifetimeSeconds) {}
    public record UserView(long id, String login, UserRole role, LocalDateTime createdAt) {}
}
