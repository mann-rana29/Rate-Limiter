package main.rate_limiter.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${rate-limiter.window-seconds}")
    private long windowSeconds;

    @Value("${rate-limiter.max-requests}")
    private int  maxRequests;

    public boolean isAllowedFixedWindow(String apiKey){
        long currentTimestamp = java.time.Instant.now().getEpochSecond();

        long windowId = currentTimestamp/windowSeconds;

        String key = "rate:fixed:" + apiKey + ":" + windowId ;

        Boolean isNewWindow = redisTemplate.opsForValue().setIfAbsent(
                key, "1" , Duration.ofSeconds(windowSeconds)
        );

        if(isNewWindow != null && isNewWindow) return true;

        Long currCount = redisTemplate.opsForValue().increment(key);

        if(currCount != null && currCount > maxRequests) return false;

        return true;

    }
}
