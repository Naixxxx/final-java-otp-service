package dev.naixxxx.guardcode.service;

import dev.naixxxx.guardcode.dao.UserRepository;
import dev.naixxxx.guardcode.domain.AppUser;
import dev.naixxxx.guardcode.domain.UserRole;
import dev.naixxxx.guardcode.dto.AuthDtos;
import dev.naixxxx.guardcode.security.JwtService;
import dev.naixxxx.guardcode.security.Passwords;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthFacadeTest {
    @Test
    void blocksSecondAdmin() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsAdmin()).thenReturn(true);
        AuthFacade auth = new AuthFacade(repo, new Passwords(), new JwtService("very-long-test-secret-key", 10));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> auth.register(new AuthDtos.RegisterRequest("root2", "secret123", UserRole.ADMIN)));
        assertEquals(409, ex.httpStatus());
        verify(repo, never()).create(any(), any(), any());
    }

    @Test
    void returnsTokenOnSuccessfulLogin() {
        Passwords passwords = new Passwords();
        String hash = passwords.hash("secret123");
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByLogin("ivan")).thenReturn(Optional.of(new AppUser(1, "ivan", hash, UserRole.USER, LocalDateTime.now())));
        AuthFacade auth = new AuthFacade(repo, passwords, new JwtService("very-long-test-secret-key", 10));
        var token = auth.login(new AuthDtos.LoginRequest("ivan", "secret123"));
        assertEquals("Bearer", token.tokenType());
        assertNotNull(token.token());
    }
}
