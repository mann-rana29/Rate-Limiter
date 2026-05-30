package main.rate_limiter.Services;

import main.rate_limiter.Models.RateLimitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> slidingWindowScript;

    public RateLimiterService(StringRedisTemplate redisTemplate, RedisScript<Long> slidingWindowScript){
        this.redisTemplate =redisTemplate;
        this.slidingWindowScript = slidingWindowScript;
    }

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

    public RateLimitStatus isAllowedSlidingWindow(String apiKey){
        String key = "rate:sliding" + apiKey;

        long nowMs = System.currentTimeMillis();
        long windowMs = windowSeconds*1000;
        String requestId = UUID.randomUUID().toString();

        Long result = redisTemplate.execute(
                slidingWindowScript,
                Collections.singletonList(key),
                String.valueOf(nowMs),
                String.valueOf(windowMs),
                String.valueOf(maxRequests),
                requestId
        );

        boolean allowed = (result != null && result == 1);

        Long currCount = redisTemplate.opsForZSet().zCard(key);
        long currCountVal = (currCount != null) ? currCount : 0L;

        long remainingRequests = (maxRequests - currCountVal > 0) ? maxRequests - currCountVal : 0L;

        return new RateLimitStatus(allowed, 45 ,(int) remainingRequests);
    }

}
