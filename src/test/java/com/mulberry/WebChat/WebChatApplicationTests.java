package com.mulberry.WebChat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class WebChatApplicationTests {
    @Autowired
    StringRedisTemplate template;

    @Test
    void contextLoads() {
        System.out.println("hello");
        String a = template.opsForValue().get("user:1001");
        System.out.println("a = " + a);
    }

}
