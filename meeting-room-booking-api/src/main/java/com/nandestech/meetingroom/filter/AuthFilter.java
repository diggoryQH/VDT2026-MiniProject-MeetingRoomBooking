package com.nandestech.meetingroom.filter;

import tools.jackson.databind.ObjectMapper;
import com.nandestech.meetingroom.dto.ApiResponse;
import com.nandestech.meetingroom.service.PasetoTokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthFilter implements Filter {

    @Autowired
    private PasetoTokenService pasetoTokenService;

    @Autowired
    private com.nandestech.meetingroom.repository.UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Roles with full administrative access
    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "SUPERADMIN");

    // Roles that can approve/reject bookings
    private static final Set<String> APPROVER_ROLES = Set.of("ADMIN", "SUPERADMIN", "APPROVER");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Skip auth for login, refresh, and swagger endpoints
        // Also skip OPTIONS requests for CORS preflight
        if (method.equalsIgnoreCase("OPTIONS")
                || path.startsWith("/api/v1/auth")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(httpResponse);
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = pasetoTokenService.verifyToken(token);
            com.nandestech.meetingroom.entity.User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            httpRequest.setAttribute("X-Username", username);
            httpRequest.setAttribute("X-Role", user.getRole());
            httpRequest.setAttribute("X-UserId", user.getId());
            httpRequest.setAttribute("X-DepartmentId", user.getDepartmentId());

            String role = user.getRole();

            // ── User management: ADMIN only (except current user info and directory) ──
            if (path.startsWith("/api/v1/users") && !path.equals("/api/v1/users/current") && !path.equals("/api/v1/users/directory")) {
                if (!ADMIN_ROLES.contains(role)) {
                    sendForbiddenResponse(httpResponse);
                    return;
                }
            }

            // ── Room management (create/update/delete): ADMIN only ──
            if (path.startsWith("/api/v1/rooms") && !method.equalsIgnoreCase("GET")) {
                if (!ADMIN_ROLES.contains(role)) {
                    sendForbiddenResponse(httpResponse);
                    return;
                }
            }

            // ── Booking approval/rejection: ADMIN or APPROVER only ──
            if (path.matches("/api/v1/bookings/\\d+/(approve|reject)")) {
                if (!APPROVER_ROLES.contains(role)) {
                    sendForbiddenResponse(httpResponse);
                    return;
                }
            }

            // ── Export: All roles ──
            // if (path.startsWith("/api/v1/export")) { ... }

            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            sendUnauthorizedResponse(httpResponse);
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status("unauthorized")
                .message("Unauthorized")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private void sendForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status("forbidden")
                .message("Access denied: insufficient permissions")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
