package com.mulberry.WebChat.interceptor;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate template;

    public WebSocketAuthInterceptor(
            JwtUtil jwtUtil,
            StringRedisTemplate template
    ) {
        this.jwtUtil = jwtUtil;
        this.template = template;
    }

    private String extractToken(ServerHttpRequest request) {
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        return null;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        try {
            String token = extractToken(request);
            if (token != null) {
                String username = jwtUtil.extractUnexpiredUsername(token);
                if (username != null) {
                    System.out.println("username is null");
                    if (template.opsForValue().get(CommonConst.USER_ROLE_PREFIX + username) != null) {
                        attributes.put("username", username);
                        return true;
                    }
                }
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
