package dev.naixxxx.guardcode.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.naixxxx.guardcode.domain.UserRole;

import java.time.Instant;
import java.util.Date;

public class JwtService {
    private final Algorithm algorithm;
    private final long ttlSeconds;

    public JwtService(String secret, long ttlMinutes) {
        if (secret == null || secret.length() < 16) throw new IllegalArgumentException("JWT secret must be at least 16 chars");
        this.algorithm = Algorithm.HMAC256(secret);
        this.ttlSeconds = ttlMinutes * 60;
    }

    public String issue(SessionUser user) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(user.id()))
                .withClaim("login", user.login())
                .withClaim("role", user.role().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

    public SessionUser verify(String token) {
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return new SessionUser(
                Long.parseLong(jwt.getSubject()),
                jwt.getClaim("login").asString(),
                UserRole.valueOf(jwt.getClaim("role").asString())
        );
    }

    public long ttlSeconds() { return ttlSeconds; }
}
