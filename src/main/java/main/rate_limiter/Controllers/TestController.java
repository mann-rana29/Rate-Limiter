package main.rate_limiter.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private RedisTemplate<String , String> redisTemplate;

    @GetMapping("/test/redis")
    public String test(){
        redisTemplate.opsForValue().set("Hello","World");

        return redisTemplate.opsForValue().get("Hello");
    }

}
