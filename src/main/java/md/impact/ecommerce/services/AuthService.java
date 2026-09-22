package md.impact.ecommerce.services;

import md.impact.ecommerce.dtos.AuthResponse;
import md.impact.ecommerce.dtos.LoginRequest;
import md.impact.ecommerce.dtos.MeResponse;
import md.impact.ecommerce.dtos.RefreshRequest;
import md.impact.ecommerce.dtos.RegisterRequest;
import md.impact.ecommerce.entities.RefreshToken;
import md.impact.ecommerce.entities.Role;
import md.impact.ecommerce.entities.User;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.RefreshTokenRepository;
import md.impact.ecommerce.repositories.UserRepository;
import md.impact.ecommerce.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshDays;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.jwt.refresh-days:30}") long refreshDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshDays = refreshDays;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw ApiException.conflict("Există deja un cont cu acest email");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), Role.USER);
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> ApiException.unauthorized("Email sau parolă incorecte"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Email sau parolă incorecte");
        }
        return toResponse(user);
    }

    // Lecția 11 — access tokenul (JWT) expiră repede (120 min, Lecția 2), ca să
    // limiteze cât rămâne valid dacă e furat. Refresh tokenul trăiește mult mai
    // mult (30 de zile), dar e o valoare opacă, ținută în refresh_tokens — nu un
    // JWT — tocmai ca să poată fi REVOCAT (logout, o parolă schimbată, un abuz
    // detectat). Un JWT stateless nu poate fi „uitat" înainte de expirare; un
    // rând în baza de date, da.
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken existing = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> ApiException.unauthorized("Refresh token invalid"));
        if (existing.isRevoked() || existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ApiException.unauthorized("Refresh token expirat sau revocat");
        }
        // Rotație: fiecare refresh invalidează tokenul folosit și emite unul nou —
        // dacă un refresh token furat e reutilizat DUPĂ ce proprietarul de drept
        // l-a folosit deja, cel vechi e deja revocat și nu mai funcționează.
        existing.revoke();
        return toResponse(existing.getUser());
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenRepository.findByToken(req.refreshToken()).ifPresent(RefreshToken::revoke);
    }

    @Transactional(readOnly = true)
    public MeResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Email sau parolă incorecte"));
        return new MeResponse(user.getEmail(), user.getRole().name());
    }

    private AuthResponse toResponse(User user) {
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(new RefreshToken(
                refreshToken, user, LocalDateTime.now().plusDays(refreshDays)));
        return new AuthResponse(jwtService.generate(user), refreshToken, user.getEmail(), user.getRole().name());
    }
}
