package com.mulberry.WebChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableWebSocketMessageBroker
@PropertySource("classpath:secret.properties")
public class WebChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebChatApplication.class, args);
    }

}
