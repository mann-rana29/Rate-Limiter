package main.rate_limiter.Controllers;

import main.rate_limiter.Services.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test/redis")
    public String test(){
        return "hello world";
    }

    @GetMapping("/test/data")
    public ResponseEntity<Map<String , Object>> data(){
        return ResponseEntity.ok(Map.of("message",  "Data fetched successfully", "timestamp",java.time.Instant.now()));
    }

    @GetMapping("/test/user/{id}")
    public ResponseEntity<Map<String,Object>> user(@PathVariable String id){
        return ResponseEntity.ok(Map.of("userId",id, "name" , "test user"));
    }

    @GetMapping("/test/status")
    public ResponseEntity<Map<String,Object>> status(){
        return ResponseEntity.ok(Map.of("status" ,"OK", "service" , "Rate Limiter API"));
    }
}
