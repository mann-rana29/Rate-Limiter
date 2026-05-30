-- KEYS[1]: The Redis rate limit key (e.g., rate:sliding:user123)
-- ARGV[1]: Current timestamp in milliseconds
-- ARGV[2]: Window size in milliseconds (e.g., 60000 for 1 minute)
-- ARGV[3]: Max allowed requests (e.g., 10)
-- ARGV[4]: Unique request ID (UUID string)

local rateLimitKey = KEYS[1]
local now = tonumber(ARGV[1])
local windowMs = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])
local requestId = ARGV[4]

local clearBefore = now - windowMs

redis.call('ZREMRANGEBYSCORE', rateLimitKey, 0, clearBefore)

local currentRequests = redis.call('ZCARD', rateLimitKey)

if currentRequests >= maxRequests then
    return 0
else
    redis.call('ZADD', rateLimitKey, now, requestId)

    local expirySeconds = math.ceil(windowMs/1000)
    redis.call('EXPIRE',rateLimitKey, expirySeconds)

    return 1

end

