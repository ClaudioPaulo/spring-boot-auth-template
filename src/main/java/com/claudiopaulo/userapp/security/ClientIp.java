package com.claudiopaulo.userapp.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client address for logging and auditing.
 *
 * <p>It uses the address of the TCP connection and deliberately ignores the X-Forwarded-For header,
 * because any client can send that header and forge the address that ends up in the audit trail.
 * Behind a trusted reverse proxy, enable {@code server.forward-headers-strategy} so Spring resolves
 * the real client address and {@link HttpServletRequest#getRemoteAddr()} returns it.
 */
public final class ClientIp {

    private ClientIp() {
    }

    public static String from(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
