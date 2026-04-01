package com.axioquan.payment_service.modules.auth;

import com.axioquan.payment_service.config.JwtTokenProvider;
import com.axioquan.payment_service.domain.entities.User;
import com.axioquan.payment_service.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "JWT token generation for testing")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Generate JWT token for testing
     * Usage: POST /api/v1/auth/generate-token?userId=<user-id>
     */
    @PostMapping("/generate-token")
    @Operation(
            summary = "Generate JWT Token (Testing Only)",
            description = "Generate a JWT token for an existing user by UUID. Used for Postman testing."
    )
    public ResponseEntity<ApiResponse<?>> generateToken(
            @RequestParam UUID userId) {

        log.info("Generating JWT token for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("name", user.getName());
        data.put("expiresIn", "7 days");

        return ResponseEntity.ok(
                ApiResponse.success("JWT token generated successfully", data)
        );
    }

    /**
     * Generate JWT token using email (alternative method)
     * Usage: POST /api/v1/auth/generate-token-by-email?email=<email>
     */
    @PostMapping("/generate-token-by-email")
    @Operation(
            summary = "Generate JWT Token by Email (Testing Only)",
            description = "Generate a JWT token using user email. Used for Postman testing."
    )
    public ResponseEntity<ApiResponse<?>> generateTokenByEmail(
            @RequestParam String email) {

        log.info("Generating JWT token for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User with email '" + email + "' not found"));
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("name", user.getName());
        data.put("expiresIn", "7 days");

        return ResponseEntity.ok(
                ApiResponse.success("JWT token generated successfully", data)
        );
    }

    /**
     * Validate JWT token
     * Usage: POST /api/v1/auth/validate-token?token=<jwt-token>
     */
    @PostMapping("/validate-token")
    @Operation(
            summary = "Validate JWT Token",
            description = "Check if a JWT token is valid"
    )
    public ResponseEntity<ApiResponse<?>> validateToken(
            @RequestParam String token) {

        boolean isValid = jwtTokenProvider.validateToken(token);
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        Map<String, Object> data = new HashMap<>();
        data.put("valid", isValid);
        data.put("expired", isExpired);

        if (isValid && !isExpired) {
            data.put("userId", jwtTokenProvider.getUserIdFromToken(token));
            data.put("email", jwtTokenProvider.getEmailFromToken(token));
            data.put("role", jwtTokenProvider.getRoleFromToken(token));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Token validated", data)
        );
    }

    /**
     * Generate JWT token with custom role (for testing different roles)
     * Usage: POST /api/v1/auth/generate-token-with-role?email=<email>&role=<role>
     * Roles: student, instructor, admin
     */
    @PostMapping("/generate-token-with-role")
    @Operation(
            summary = "Generate JWT Token with Custom Role",
            description = "Generate a JWT token with a specific role. Used for testing role-based endpoints. Roles: student, instructor, admin"
    )
    public ResponseEntity<ApiResponse<?>> generateTokenWithRole(
            @RequestParam String email,
            @RequestParam(required = false) String role) {

        log.info("Generating JWT token for email: {} with role: {}", email, role);

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User with email '" + email + "' not found"));
        }

        // Use provided role if specified, otherwise use default role
        String roleToUse = (role != null && !role.isEmpty()) ? role : "STUDENT";

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName(), roleToUse);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("name", user.getName());
        data.put("role", roleToUse);
        data.put("expiresIn", "7 days");

        return ResponseEntity.ok(
                ApiResponse.success("JWT token generated successfully with role", data)
        );
    }
}
