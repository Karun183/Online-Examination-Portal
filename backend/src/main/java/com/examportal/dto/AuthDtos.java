package com.examportal.dto;
import com.examportal.model.Role; import jakarta.validation.constraints.*;
public final class AuthDtos {
 private AuthDtos() {}
 public record SignupRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min=6) String password) {}
 public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
 public record AuthResponse(String token, String name, String email, Role role) {}
 public record ProfileResponse(String name, String email, Role role) {}
}
