package com.pedrocoelho.microservices.core.recommendation.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RecommendationRepository extends PagingAndSortingRepository<RecommendationEntity, String> {
  List<RecommendationEntity> findByProductId(int productId);
  List<RecommendationEntity> findByRecommendationId(int productId);
}
