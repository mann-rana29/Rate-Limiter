package main.rate_limiter.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitStatus {
    private boolean isAllowed;
    private long remainingTimeInSeconds;
    private int remainingRequests;
}
