package com.mulberry.WebChat.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate template;

    public JwtAuthenticationFilter(
            ObjectMapper objectMapper,
            JwtUtil jwtUtil,
            StringRedisTemplate template
    ) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.template = template;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header =request.getHeader("Authorization");
        if (header == null || !header.startsWith(CommonConst.OAUTH_TOKEN)) {
            sendMessage(response, "No authorization token");
            return;
        }

        String token = header.substring(CommonConst.OAUTH_LENGTH);
        try {
            String username = jwtUtil.extractUnexpiredUsername(token);
            if (username == null) {
                sendMessage(response, "Token expired or invalid");
                return;
            }

            String role = template.opsForValue().get(CommonConst.USER_ROLE_PREFIX + username);
            if (role == null) {
                sendMessage(response, "Current user has logout");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("")
                        .authorities(role)
                        .build();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            sendMessage(response, "Malformed or invalid token");
        }

        filterChain.doFilter(request, response);
    }

    public void sendMessage(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.error(message)));
    }
}
