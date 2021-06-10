package com.faza.example.springcloudgatewaycustomapilimiter.service.impl;

import com.faza.example.springcloudgatewaycustomapilimiter.helper.ObjectHelper;
import com.faza.example.springcloudgatewaycustomapilimiter.model.constant.CacheKey;
import com.faza.example.springcloudgatewaycustomapilimiter.model.database.ApiLimiter;
import com.faza.example.springcloudgatewaycustomapilimiter.model.dto.ApiRateLimiterCacheDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiRateLimiter implements RateLimiter<Object> {

  private static final String REMAINING_TIME_IN_SECONDS_HEADER =
      "X-RateLimit-Remaining-Time-In-Seconds";
  private static final String TIME_IN_SECONDS_HEADER =
      "X-RateLimit-Time-In-Seconds";

  @Autowired
  private ObjectHelper objectHelper;

  @Autowired
  private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

  @Override
  public Mono<Response> isAllowed(String routeId, String id) {
    ApiLimiter apiLimiter = (ApiLimiter) objectHelper.fromStringBase64(id);
    ApiRateLimiterCacheDto apiRateLimiterCacheDto = ApiRateLimiterCacheDto.builder()
        .cacheKey(buildApiRateLimiterCacheKey(routeId, apiLimiter))
        .threshold(apiLimiter.getThreshold())
        .ttl(apiLimiter.getTtl())
        .build();
    return reactiveStringRedisTemplate.opsForValue()
        .increment(apiRateLimiterCacheDto.getCacheKey())
        .doOnNext(apiRateLimiterCacheDto::setRate)
        .flatMap(rate -> setCacheExpiration(apiRateLimiterCacheDto))
        .flatMap(this::setRemainingTtl)
        .handle(this::handleApiRateLimiterCacheValue);
  }

  @Override
  public Map<String, Object> getConfig() {
    return null;
  }

  @Override
  public Class<Object> getConfigClass() {
    return null;
  }

  @Override
  public Object newConfig() {
    return null;
  }

  private String buildApiRateLimiterCacheKey(String routeId, ApiLimiter apiLimiter) {
    return String.join(CacheKey.CACHE_KEY_SEPARATOR, CacheKey.API_RATE_LIMITER_CACHE_KEY_PREFIX,
        routeId, apiLimiter.getPath(), apiLimiter.getMethod());
  }

  private Mono<ApiRateLimiterCacheDto> setCacheExpiration(ApiRateLimiterCacheDto apiRateLimiterCacheDto) {
    return Mono.just(apiRateLimiterCacheDto.getRate())
        .filter(rate -> rate == 1)
        .flatMap(rate -> reactiveStringRedisTemplate.expire(
            apiRateLimiterCacheDto.getCacheKey(),
            Duration.ofSeconds(apiRateLimiterCacheDto.getTtl())))
        .thenReturn(apiRateLimiterCacheDto);
  }

  private Mono<ApiRateLimiterCacheDto> setRemainingTtl(ApiRateLimiterCacheDto apiRateLimiterCacheDto) {
    return reactiveStringRedisTemplate.getExpire(apiRateLimiterCacheDto.getCacheKey())
        .doOnNext(duration -> apiRateLimiterCacheDto.setRemainingTtl(duration.getSeconds()))
        .thenReturn(apiRateLimiterCacheDto);
  }

  private void handleApiRateLimiterCacheValue(ApiRateLimiterCacheDto apiRateLimiterCacheDto,
      SynchronousSink<Response> synchronousSink) {
    long remaining = apiRateLimiterCacheDto.getThreshold() - apiRateLimiterCacheDto.getRate();
    synchronousSink.next(
        new Response(isAllowed(remaining), getHeaders(remaining, apiRateLimiterCacheDto)));
  }

  private boolean isAllowed(long remaining) {
    return remaining >= 0;
  }

  public Map<String, String> getHeaders(long remaining,
      ApiRateLimiterCacheDto apiRateLimiterCacheDto) {
    Map<String, String> headers = new HashMap<>();
    headers.put(RedisRateLimiter.REMAINING_HEADER,
        String.valueOf(getRemainingHeaderValue(remaining)));
    headers.put(RedisRateLimiter.REPLENISH_RATE_HEADER,
        String.valueOf(apiRateLimiterCacheDto.getThreshold()));
    headers.put(REMAINING_TIME_IN_SECONDS_HEADER,
        String.valueOf(apiRateLimiterCacheDto.getRemainingTtl()));
    headers.put(TIME_IN_SECONDS_HEADER, String.valueOf(apiRateLimiterCacheDto.getTtl()));
    return headers;
  }

  private long getRemainingHeaderValue(long remaining) {
    if (remaining < 0) {
      return 0;
    }
    return remaining;
  }
}
