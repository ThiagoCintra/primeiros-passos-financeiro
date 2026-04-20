package com.primeirospassos.financeiro.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import javax.crypto.spec.SecretKeySpec;

import com.primeirospassos.financeiro.security.RateLimitFilter;
import com.primeirospassos.financeiro.security.UserContextFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserContextFilter userContextFilter, RateLimitFilter rateLimitFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/financeiro/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(this::convertJwt)))
                .addFilterAfter(userContextFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, UserContextFilter.class);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.security.jwt-secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
    }

    private AbstractAuthenticationToken convertJwt(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
