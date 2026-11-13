package ra.edu.hw.md04.ss14.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ra.edu.hw.md04.ss14.entity.RefreshToken;
import ra.edu.hw.md04.ss14.entity.User;
import ra.edu.hw.md04.ss14.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshTokenExpiration),
                user
        );
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token is invalid or already used"));
        if (!refreshToken.getExpiryDate().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expired");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken currentToken = verifyExpiration(token);
        User user = currentToken.getUser();
        int consumed = refreshTokenRepository.consumeValidToken(token, Instant.now());
        if (consumed != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token is invalid or already used");
        }
        return createRefreshToken(user);
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}
