package com.mulberry.WebChat.interceptor;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.util.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command) ||
                StompCommand.SEND.equals(command) ||
                StompCommand.SUBSCRIBE.equals(command)) {

            if (accessor.getUser() == null) {
                List<String> authList = accessor.getNativeHeader("Authorization");
                String token = null;
                if (authList != null && !authList.isEmpty()) {
                    String authHeader = authList.get(0);
                    if (authHeader != null && authHeader.startsWith(CommonConst.OAUTH_TOKEN)) {
                        token = authHeader.substring(CommonConst.OAUTH_LENGTH);
                    }
                }

                if (token == null) {
                    System.out.println("Missing or invalid Authorization header");
                    return null;
                }

                try {
                    String username = jwtUtil.extractUnexpiredUsername(token);
                    if (username == null ||
                            redisTemplate.opsForValue().get(CommonConst.USER_ROLE_PREFIX + username) == null) {
                        System.out.println("Invalid or expired token");
                        return null;
                    }

                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return username;
                        }
                    });
                    System.out.println("Successfully authenticated user: " + username + " for command: " + command);
                } catch (Exception e) {
                    System.out.println("Authentication failed: " + e.getMessage());
                }
            }
        }

        return message;
    }
}