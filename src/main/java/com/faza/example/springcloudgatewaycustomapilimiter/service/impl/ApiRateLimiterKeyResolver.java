package com.faza.example.springcloudgatewaycustomapilimiter.service.impl;

import com.faza.example.springcloudgatewaycustomapilimiter.helper.ObjectHelper;
import com.faza.example.springcloudgatewaycustomapilimiter.service.ApiLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
public class ApiRateLimiterKeyResolver implements KeyResolver {
  
  @Autowired
  private ApiLimiterService apiLimiterService;
  
  @Autowired
  private ObjectHelper objectHelper;

  @Override
  public Mono<String> resolve(ServerWebExchange exchange) {
    return apiLimiterService.findMatchesApiLimiter(
          exchange.getRequest()
              .getPath()
              .value(), 
          exchange.getRequest()
              .getMethodValue())
        .map(objectHelper::toStringBase64);
  }
}
