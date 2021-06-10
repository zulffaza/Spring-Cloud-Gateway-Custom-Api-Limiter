package com.faza.example.springcloudgatewaycustomapilimiter.service;

import com.faza.example.springcloudgatewaycustomapilimiter.model.database.ApiLimiter;
import com.faza.example.springcloudgatewaycustomapilimiter.model.web.CreateOrUpdateApiLimiter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApiLimiterService {

  Flux<ApiLimiter> findApiLimiters();

  Mono<ApiLimiter> findApiLimiter(Long id);

  Mono<Void> createApiLimiter(CreateOrUpdateApiLimiter createOrUpdateApiLimiter);

  Mono<Void> updateApiLimiter(Long id, CreateOrUpdateApiLimiter createOrUpdateApiLimiter);

  Mono<Void> deleteApiLimiter(Long id);
  
  Mono<Void> updateActivationStatus(Long id, boolean activate);
  
  Mono<ApiLimiter> findMatchesApiLimiter(String path, String method);
}
