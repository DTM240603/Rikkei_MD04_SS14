package ra.edu.hw.md04.ss14.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import ra.edu.hw.md04.ss14.dto.LoginRequest;
import ra.edu.hw.md04.ss14.dto.RegisterRequest;
import ra.edu.hw.md04.ss14.dto.TokenResponse;
import ra.edu.hw.md04.ss14.entity.RefreshToken;
import ra.edu.hw.md04.ss14.entity.User;
import ra.edu.hw.md04.ss14.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        validateCredentials(request == null ? null : request.username(), request == null ? null : request.password());
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = userRepository.save(
                new User(request.username(), passwordEncoder.encode(request.password()), "ROLE_USER")
        );
        return createTokenResponse(user, refreshTokenService.createRefreshToken(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        validateCredentials(request == null ? null : request.username(), request == null ? null : request.password());
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return createTokenResponse(user, refreshTokenService.createRefreshToken(user));
    }

    @Transactional
    public TokenResponse refresh(String token) {
        validateRefreshToken(token);
        RefreshToken refreshToken = refreshTokenService.rotate(token);
        return createTokenResponse(refreshToken.getUser(), refreshToken);
    }

    @Transactional
    public void logout(String token) {
        validateRefreshToken(token);
        refreshTokenService.revoke(token);
    }

    private void validateCredentials(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
    }

    private void validateRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
    }

    private TokenResponse createTokenResponse(User user, RefreshToken refreshToken) {
        return new TokenResponse(
                jwtUtils.generateAccessToken(user),
                refreshToken.getToken(),
                "Bearer",
                jwtUtils.getAccessTokenExpiration()
        );
    }
}
