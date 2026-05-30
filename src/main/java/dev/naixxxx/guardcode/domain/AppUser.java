package dev.naixxxx.guardcode.domain;

import java.time.LocalDateTime;

public record AppUser(long id, String login, String passwordHash, UserRole role, LocalDateTime createdAt) {}
