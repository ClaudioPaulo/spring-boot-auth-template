package com.claudiopaulo.userapp.controller;

import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.dto.UserResponse;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.security.ClientIp;
import com.claudiopaulo.userapp.security.SecurityAuditLogger;
import com.claudiopaulo.userapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management endpoints (CRUD)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    private final SecurityAuditLogger auditLogger;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "List all users",
        description = "Returns the full list of registered users. Requires the ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User list returned successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied: administrators only",
            content = @Content
        )
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        String currentUser = getCurrentUsername();
        log.info("Admin {} requested list of all users", currentUser);
        
        List<UserResponse> users = userService.getAllUsers();
        log.debug("Returning {} users to admin {}", users.size(), currentUser);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id)")
    @Operation(
        summary = "Get user by ID",
        description = "Returns a single user. Admins can view any user; regular users can only view their own data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado",
            content = @Content
        )
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        
        String currentUser = getCurrentUsername();
        log.info("User {} requested details of user ID: {}", currentUser, id);
        
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id)")
    @Operation(
        summary = "Update user",
        description = "Updates a user. Admins can update any user; regular users can only update their own data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or duplicate data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado",
            content = @Content
        )
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id, 
            @Valid @RequestBody RegisterRequest request) {
        
        String currentUser = getCurrentUsername();
        log.info("User {} attempting to update user ID: {}", currentUser, id);
        
        UserResponse updatedUser = userService.updateUser(id, request);
        log.info("User {} successfully updated user ID: {}", currentUser, id);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete user",
        description = "Removes a user from the system. Requires the ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied: administrators only",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String currentUser = getCurrentUsername();
        log.warn("Admin {} attempting to delete user ID: {}", currentUser, id);
        
        userService.deleteUser(id);
        auditLogger.logUserDeletion(currentUser, id, ClientIp.from(request));
        log.warn("Admin {} successfully deleted user ID: {}", currentUser, id);
        
        return ResponseEntity.noContent().build();
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getUsername();
        }
        return "anonymous";
    }
}