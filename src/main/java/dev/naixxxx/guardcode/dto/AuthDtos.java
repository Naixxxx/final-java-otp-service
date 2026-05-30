package dev.naixxxx.guardcode.dto;

import dev.naixxxx.guardcode.domain.UserRole;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(String login, String password, UserRole role) {}
    public record LoginRequest(String login, String password) {}
    public record TokenResponse(String token, String tokenType, long expiresInSeconds) {}
}
