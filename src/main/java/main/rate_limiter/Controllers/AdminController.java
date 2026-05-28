package main.rate_limiter.Controllers;

import main.rate_limiter.Services.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Value("${rate-limiter.max-requests}")
    private int maxRequests;

    @GetMapping("/admin/rate-limit/{apiKey}")
    public ResponseEntity<Map<String,Object>> rateLimit(@PathVariable String apiKey){
        int currRequestCount = rateLimiterService.getRemainingRequests(apiKey);

        return ResponseEntity.ok(Map.of("Current Request Count" , currRequestCount));
    }

    @DeleteMapping("/admin/rate-limit/{apiKey}")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable String apiKey){
        rateLimiterService.resetRateLimit(apiKey);
        return ResponseEntity.ok(Map.of("message" , "Rate limit reset successfully"));
    }
}
