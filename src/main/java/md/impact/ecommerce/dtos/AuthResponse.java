package md.impact.ecommerce.dtos;

public record AuthResponse(String token, String refreshToken, String email, String role) {
}
