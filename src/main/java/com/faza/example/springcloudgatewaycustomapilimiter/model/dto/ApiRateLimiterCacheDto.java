package com.faza.example.springcloudgatewaycustomapilimiter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRateLimiterCacheDto {
  
  private String cacheKey;

  private long rate;
  private long remainingTtl;

  private int threshold;
  private int ttl;
}
