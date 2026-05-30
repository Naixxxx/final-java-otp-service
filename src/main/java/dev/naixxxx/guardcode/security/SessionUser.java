package dev.naixxxx.guardcode.security;

import dev.naixxxx.guardcode.domain.UserRole;

public record SessionUser(long id, String login, UserRole role) {}
