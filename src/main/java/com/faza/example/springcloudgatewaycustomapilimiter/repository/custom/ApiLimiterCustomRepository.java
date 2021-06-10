package com.faza.example.springcloudgatewaycustomapilimiter.repository.custom;

import com.faza.example.springcloudgatewaycustomapilimiter.model.database.ApiLimiter;
import reactor.core.publisher.Mono;

public interface ApiLimiterCustomRepository {

  Mono<ApiLimiter> findMatchesApiLimiter(String path, String method);
}
