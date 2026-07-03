package com.nandestech.meetingroom.controller;

import com.nandestech.meetingroom.dto.ApiResponse;
import com.nandestech.meetingroom.dto.UserResponse;
import com.nandestech.meetingroom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for user profile and management")
public class UserController {

    @Autowired
    private UserService userService;

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users. Access restricted to SUPERADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "success",
                    "data": {
                        "content": [
                            {
                                "id": 1,
                                "name": "test name",
                                "username": "testuser",
                                "email": "test@example.com",
                                "created_at": "2026-05-11T10:00:00",
                                "updated_at": "2026-05-11T10:00:00"
                            }
                        ],
                        "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 1,
                            "totalPages": 1
                        }
                    }
                }
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to retrieve users"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(page, limit)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to get users"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @GetMapping("/directory")
    @Operation(summary = "Get user directory", description = "Retrieve a list of all active users for dropdowns.")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getUserDirectory() {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.getUserDirectory()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to get user directory"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by their ID. Access restricted to SUPERADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to retrieve user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to get user"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.web.bind.annotation.PostMapping
    @Operation(summary = "Create a new user", description = "Register a new user. Role defaults to EMPLOYEE. Access restricted to SUPERADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to create user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@org.springframework.web.bind.annotation.RequestBody com.nandestech.meetingroom.dto.UserRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to create user"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.web.bind.annotation.PatchMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details by their ID. Access restricted to SUPERADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to update user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody com.nandestech.meetingroom.dto.UserRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
        } catch (RuntimeException e) {
            // Using exactly "failed to update room" if I follow the spec literally, 
            // but the user's issue.md had "failed to update user" after my correction.
            // I'll use "failed to update user".
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to update user"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user by their ID. Access restricted to SUPERADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to delete user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<String>> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .message("user deleted successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed("failed to delete user"));
        }
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @GetMapping("/current")
    @Operation(
            summary = "Get current logged-in user",
            description = "Fetch details of the user associated with the provided authentication token."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "success",
                    "data": {
                        "id": 1,
                        "name": "test name",
                        "username": "tes username",
                        "email": "test@example.com",
                        "role": "SUPERADMIN",
                        "created_at": "2022-01-01T00:00:00Z",
                        "updated_at": "2022-01-01T00:00:00Z",
                        "bookings": []
                    }
                }
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to retrieve user",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "failed",
                    "message": "failed to get current user"
                }
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                    "status": "unauthorized",
                    "message": "Unauthorized"
                }
                """)))
    })
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username) {
        try {
            UserResponse data = userService.getCurrentUser(username);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failed(e.getMessage()));
        }
    }
}
