package md.impact.ecommerce.services;

import md.impact.ecommerce.dtos.AuthResponse;
import md.impact.ecommerce.dtos.LoginRequest;
import md.impact.ecommerce.dtos.MeResponse;
import md.impact.ecommerce.dtos.RegisterRequest;
import md.impact.ecommerce.entities.Role;
import md.impact.ecommerce.entities.User;
import md.impact.ecommerce.exceptions.ApiException;
import md.impact.ecommerce.repositories.UserRepository;
import md.impact.ecommerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Lecția 8 — teste unitare "adevărate": spre deosebire de ApiIntegrationTest
 * (@SpringBootTest, context Spring real, bază de date H2), aici NU pornim
 * niciun context Spring și NU atingem nicio bază de date — doar mock-uim
 * dependențele lui AuthService (UserRepository, PasswordEncoder, JwtService)
 * cu Mockito și verificăm logica serviciului izolat. Rulează în milisecunde.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    AuthService authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_hashes_the_password_before_saving() {
        var req = new RegisterRequest("elev@impact.md", "parola-in-clar");
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("$2b$10$hash-fals-pentru-test");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generate(any(User.class))).thenReturn("jwt-fals");

        AuthResponse response = authService.register(req);

        // ArgumentCaptor: prindem exact User-ul trimis spre save() ca să confirmăm
        // că parola SALVATĂ e hash-ul, nu parola în clar primită în request.
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2b$10$hash-fals-pentru-test");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo(req.password());

        assertThat(response.email()).isEqualTo("elev@impact.md");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.token()).isEqualTo("jwt-fals");
    }

    @Test
    void register_rejects_an_email_that_already_exists() {
        var req = new RegisterRequest("deja@impact.md", "orice");
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        // Cel mai important: dacă email-ul există deja, NU trebuie să ajungem
        // niciodată la save() — altfel am crea un al doilea cont pe același email.
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_with_correct_password_returns_a_token() {
        var req = new LoginRequest("user@impact.md", "user123");
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("user@impact.md");
        when(user.getPasswordHash()).thenReturn("hash-stocat");
        when(user.getRole()).thenReturn(Role.USER);
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), "hash-stocat")).thenReturn(true);
        when(jwtService.generate(user)).thenReturn("jwt-fals");

        AuthResponse response = authService.login(req);

        assertThat(response.token()).isEqualTo("jwt-fals");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void login_with_wrong_password_is_rejected_without_leaking_which_field_was_wrong() {
        var req = new LoginRequest("user@impact.md", "parola-gresita");
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("hash-stocat");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), "hash-stocat")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_with_unknown_email_gives_the_same_error_as_wrong_password() {
        // Mesajul de eroare e identic la email necunoscut vs. parolă greșită —
        // altfel am confirma unui atacator că un email anume are cont (user enumeration).
        when(userRepository.findByEmail("nimeni@impact.md")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nimeni@impact.md", "orice")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email sau parolă incorecte");

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void me_returns_the_typed_response_for_the_authenticated_email() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("admin@impact.md");
        when(user.getRole()).thenReturn(Role.ADMIN);
        when(userRepository.findByEmail("admin@impact.md")).thenReturn(Optional.of(user));

        MeResponse response = authService.me("admin@impact.md");

        assertThat(response).isEqualTo(new MeResponse("admin@impact.md", "ADMIN"));
    }

    @Test
    void me_with_an_email_that_no_longer_exists_is_unauthorized_not_a_server_error() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me("sters@impact.md"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
