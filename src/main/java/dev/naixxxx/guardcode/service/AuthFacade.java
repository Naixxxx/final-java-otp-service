package dev.naixxxx.guardcode.service;

import dev.naixxxx.guardcode.dao.UserRepository;
import dev.naixxxx.guardcode.domain.AppUser;
import dev.naixxxx.guardcode.domain.UserRole;
import dev.naixxxx.guardcode.dto.AuthDtos;
import dev.naixxxx.guardcode.security.JwtService;
import dev.naixxxx.guardcode.security.Passwords;
import dev.naixxxx.guardcode.security.SessionUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthFacade {
    private static final Logger log = LoggerFactory.getLogger(AuthFacade.class);
    private final UserRepository users;
    private final Passwords passwords;
    private final JwtService jwt;

    public AuthFacade(UserRepository users, Passwords passwords, JwtService jwt) {
        this.users = users; this.passwords = passwords; this.jwt = jwt;
    }

    public void register(AuthDtos.RegisterRequest req) {
        validateLogin(req.login());
        UserRole role = req.role() == null ? UserRole.USER : req.role();
        if (role == UserRole.ADMIN && users.existsAdmin()) throw new ServiceException(409, "Administrator already exists");
        users.create(req.login().trim(), passwords.hash(req.password()), role);
        log.info("Registered new {}: {}", role, req.login());
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        AppUser user = users.findByLogin(req.login()).orElseThrow(() -> new ServiceException(401, "Invalid login or password"));
        if (!passwords.matches(req.password(), user.passwordHash())) throw new ServiceException(401, "Invalid login or password");
        String token = jwt.issue(new SessionUser(user.id(), user.login(), user.role()));
        log.info("User logged in: {}", user.login());
        return new AuthDtos.TokenResponse(token, "Bearer", jwt.ttlSeconds());
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.length() > 80) throw new ServiceException(400, "Invalid login");
    }
}
