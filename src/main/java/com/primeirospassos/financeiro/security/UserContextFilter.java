package com.primeirospassos.financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserContextFilter extends OncePerRequestFilter {

    private final AuthenticatedUserService authenticatedUserService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            CurrentUser user = authenticatedUserService.currentUser();
            UserContextHolder.set(user);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
