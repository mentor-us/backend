package com.hcmus.mentor.backend.controller;

import com.hcmus.mentor.backend.payload.request.AuthResponse;
import com.hcmus.mentor.backend.payload.request.LoginRequest;
import com.hcmus.mentor.backend.payload.request.RefreshTokenRequest;
import com.hcmus.mentor.backend.security.TokenProvider;
import com.hcmus.mentor.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "Auth APIs", description = "APIs for authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, TokenProvider tokenProvider, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.authService = authService;
    }

    @Operation(summary = "Login by password", description = "", tags = "Auth APIs")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthResponse subscription = authService.createToken(authentication.getName());
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Request new token", description = "", tags = "Auth APIs")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Get successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseEntity.class))))})
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String accessToken = authService.generateNewToken(request);
        if (accessToken == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(accessToken);
    }
}
