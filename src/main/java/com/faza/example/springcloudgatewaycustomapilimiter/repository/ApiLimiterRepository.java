package com.faza.example.springcloudgatewaycustomapilimiter.repository;

import com.faza.example.springcloudgatewaycustomapilimiter.model.database.ApiLimiter;
import com.faza.example.springcloudgatewaycustomapilimiter.repository.custom.ApiLimiterCustomRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApiLimiterRepository extends ReactiveCrudRepository<ApiLimiter, Long>,
    ApiLimiterCustomRepository {

}
