package main.rate_limiter.Interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.rate_limiter.Models.RateLimitStatus;
import main.rate_limiter.Services.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        String apiKey = request.getHeader("X-API-KEY");
        if(apiKey == null){
            response.sendError(SC_UNAUTHORIZED, "Missing X-API-KEY header");
            return false;
        }

        RateLimitStatus rateLimitStatus = rateLimiterService.isAllowedFixedWindow(apiKey);

        response.setHeader("X-RateLimit-Limit","10");
        response.setHeader("X-RateLimit-Remaining",String.valueOf(rateLimitStatus.getRemainingRequests()));
        response.setHeader("X-RateLimit-Reset",String.valueOf(rateLimitStatus.getRemainingTimeInSeconds()));

        if(!rateLimitStatus.isAllowed()){
            response.sendError(429,"Rate Limit Exceeded");
            return false;
        }

        return true;
    }
}
