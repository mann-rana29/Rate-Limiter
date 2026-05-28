package main.rate_limiter.Services;

import main.rate_limiter.Models.RateLimitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public RateLimitStatus isAllowedFixedWindow(String apiKey){
        long currentTimestamp = java.time.Instant.now().getEpochSecond();

        long windowId = currentTimestamp/windowSeconds;

        String key = "rate:fixed:" + apiKey + ":" + windowId ;

        Boolean isNewWindow = redisTemplate.opsForValue().setIfAbsent(
                key, "1" , Duration.ofSeconds(windowSeconds)
        );

        if(isNewWindow != null && isNewWindow){
            return new RateLimitStatus(true,windowSeconds,maxRequests-1);
        }

        Long remainingTime = redisTemplate.getExpire(key);
        Long currCount = redisTemplate.opsForValue().increment(key);

        long currCountVal = (currCount != null) ? currCount : 0x0L;
        long remainingTimeVal = (remainingTime != null && remainingTime > 0)? remainingTime : 0x0L;
        long remainingRequests = (maxRequests - currCountVal < 0) ? 0 : maxRequests - currCountVal;

        boolean allowed = maxRequests >= currCountVal;

        return new RateLimitStatus(allowed,remainingTimeVal,(int) remainingRequests);

    }

    public int getRemainingRequests(String apiKey){
        long currentTimestamp = java.time.Instant.now().getEpochSecond();

        long windowId = currentTimestamp/windowSeconds;

        String key = "rate:fixed:" + apiKey + ":" + windowId ;

        String currCountStr  = redisTemplate.opsForValue().get(key);

        if(currCountStr == null) return 0;

        return Integer.parseInt(currCountStr);
    }

    public void resetRateLimit(String apiKey){
        long currentTimestamp = java.time.Instant.now().getEpochSecond();

        long windowId = currentTimestamp/windowSeconds;

        String key = "rate:fixed:" + apiKey + ":" + windowId ;

        redisTemplate.delete(key);
    }

}
