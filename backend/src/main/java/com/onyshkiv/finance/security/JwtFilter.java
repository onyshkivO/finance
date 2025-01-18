package com.onyshkiv.finance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onyshkiv.finance.exception.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }
//todo якщо на логін, який не потребує токена відсилати запит з невалідним токеном, то тут впаде помилка, зараз бачу варіант хіба додавати якийсь whitelist,
// щоб не перевіряти токен на ті ендпроінти, але так як це будуть робитися запити з фронту, то не будуть там передавати токен на ці ендпоінти
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = jwtUtil.resolveToken(request);
            if (accessToken != null) {
                Claims claims = jwtUtil.extractAllClaims(accessToken);
                if (claims != null) {
                    String login = claims.getSubject();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(login);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            handleAuthenticationException(response);
        }
    }

    private void handleAuthenticationException(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing JWT token");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
