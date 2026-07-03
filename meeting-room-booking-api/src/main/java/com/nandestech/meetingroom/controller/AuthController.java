package com.nandestech.meetingroom.controller;

import com.nandestech.meetingroom.dto.ApiResponse;
import com.nandestech.meetingroom.dto.AuthResponseData;
import com.nandestech.meetingroom.dto.LoginRequest;
import com.nandestech.meetingroom.dto.RefreshRequest;
import com.nandestech.meetingroom.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login and token refresh endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate user with username and password. Returns access and refresh tokens."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "success",
                    "data": {
                        "access_token": "v2.local.abc123...",
                        "refresh_token": "v2.local.def456..."
                    }
                }
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "failed",
                    "message": "Invalid username or password"
                }
                """)))
    })
    public ResponseEntity<ApiResponse<AuthResponseData>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponseData data = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Token",
            description = "Exchange a valid refresh token for a new pair of access and refresh tokens."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "success",
                    "data": {
                        "access_token": "v2.local.newAccessToken...",
                        "refresh_token": "v2.local.newRefreshToken..."
                    }
                }
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "failed",
                    "message": "Invalid token"
                }
                """)))
    })
    public ResponseEntity<ApiResponse<AuthResponseData>> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            AuthResponseData data = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed(e.getMessage()));
        }
    }

}
