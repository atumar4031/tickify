package com.aasismgt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityContext {

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void saveUserContext(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        // Spring Security no longer persists the context automatically, so store it in the session explicitly.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    public SecurityContext getUserContext() {
        return SecurityContextHolder.getContext();
    }
}
