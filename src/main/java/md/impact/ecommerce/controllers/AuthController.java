package md.impact.ecommerce.controllers;

import md.impact.ecommerce.dtos.AuthResponse;
import md.impact.ecommerce.dtos.LoginRequest;
import md.impact.ecommerce.dtos.MeResponse;
import md.impact.ecommerce.dtos.RegisterRequest;
import md.impact.ecommerce.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Înregistrare, autentificare și „cine sunt eu". */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autentificare", description = "Înregistrare, login (JWT) și utilizatorul curent")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Creează un cont nou")
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Autentificare — întoarce un token JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Utilizatorul curent (necesită token)")
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}
