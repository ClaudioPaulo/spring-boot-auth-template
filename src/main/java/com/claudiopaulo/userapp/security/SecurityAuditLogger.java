package com.claudiopaulo.userapp.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityAuditLogger {
    
    public void logLoginAttempt(String username, String ip, boolean success) {
        if (success) {
            log.info("[SECURITY_AUDIT] Successful login - Username: {}, IP: {}", username, ip);
        } else {
            log.warn("[SECURITY_AUDIT] Failed login attempt - Username: {}, IP: {}", username, ip);
        }
    }
    
    public void logRegistration(String username, String email, String ip) {
        log.info("[SECURITY_AUDIT] New user registration - Username: {}, Email: {}, IP: {}", username, email, ip);
    }
    
    public void logUserDeletion(String adminUsername, Long deletedUserId, String ip) {
        log.warn("[SECURITY_AUDIT] User deletion - Admin: {}, Deleted User ID: {}, IP: {}", adminUsername, deletedUserId, ip);
    }
    
    public void logUnauthorizedAccess(String username, String resource, String ip) {
        log.warn("[SECURITY_AUDIT] Unauthorized access attempt - User: {}, Resource: {}, IP: {}", username, resource, ip);
    }
    
    public void logPasswordChange(String username, String ip) {
        log.info("[SECURITY_AUDIT] Password changed - Username: {}, IP: {}", username, ip);
    }
}
