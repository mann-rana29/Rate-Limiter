package main.rate_limiter.Controllers;

import main.rate_limiter.Services.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/test/redis")
    public String test(){

        boolean isAllowed =  rateLimiterService.isAllowedFixedWindow("Mann");

        if(!isAllowed){
            return "Too many requests chill brother!";
        }

        return "All good";
    }

}
