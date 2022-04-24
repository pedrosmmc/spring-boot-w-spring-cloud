package com.pedrocoelho.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedrocoelho.api.core.product.Product;
import com.pedrocoelho.api.core.product.ProductService;
import com.pedrocoelho.api.core.recommendation.Recommendation;
import com.pedrocoelho.api.core.recommendation.RecommendationService;
import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.api.core.review.ReviewService;
import com.pedrocoelho.api.exceptions.InvalidInputException;
import com.pedrocoelho.api.exceptions.NotFoundException;
import com.pedrocoelho.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  @Autowired
  public ProductCompositeIntegration(
      RestTemplate restTemplate,
      ObjectMapper mapper,
      @Value("${app.product-service.host}") String productServiceHost,
      @Value("${app.product-service.port}") int productServicePort,
      @Value("${app.recommendation-service.host}") String recommendationServiceHost,
      @Value("${app.recommendation-service.port}") int recommendationServicePort,
      @Value("${app.review-service.host}") String reviewServiceHost,
      @Value("${app.review-service.port}") int reviewServicePort) {

    this.restTemplate = restTemplate;
    this.mapper = mapper;

    this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
    this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
    this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review/";
  }

  @Override
  public Product createProduct(Product body) {

    try {
      String url = productServiceUrl;
      LOG.debug("Will post a new product to URL: {}", url);
      Product product = restTemplate.postForObject(url, body, Product.class);
      LOG.debug("Created a product with id: {}", product.getProductId());

      return product;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Product getProduct(int productId) {

    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Will call the getProduct API on URL: {}", url);
      Product product = restTemplate.getForObject(url, Product.class);
      LOG.debug("Found a product with id: {}", product.getProductId());

      return product;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteProduct(int productId) {

    try {
      String url = productServiceUrl + "/" + productId;
      LOG.debug("Will call the deleteProduct API on URL: {}", url);
      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {

    try {
      String url = recommendationServiceUrl;
      LOG.debug("Will post a new recommendation to URL: {}", url);

      Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
      LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());

      return recommendation;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {

    try {
      String url = recommendationServiceUrl + "?productId=" + productId;
      LOG.debug("Will call the getRecommendations API on URL: {}", url);
      List<Recommendation> recommendations = restTemplate
          .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
          .getBody();

      LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
      return recommendations;
    } catch (HttpClientErrorException ex) {
      LOG.warn("Got an exception while requesting recommendations, returning zero recommendations: {}", ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public void deleteRecommendations(int productId) {

    try {
      String url = recommendationServiceUrl + "?productId=" + productId;
      LOG.debug("Will call the deleteRecommendations API on URL: {}", url);
      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Review createReview(Review body) {

    try {
      String url = reviewServiceUrl;
      LOG.debug("Will post a new review to URL: {}", url);
      Review review = restTemplate.postForObject(url, body, Review.class);
      LOG.debug("Created a review with id: {}", review.getProductId());
      return review;
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public List<Review> getReviews(int productId) {

    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      return restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();
    } catch (HttpClientErrorException ex) {
      LOG.warn("Got an exception while requesting reviews, returning zero reviews: {}", ex.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public void deleteReviews(int productId) {

    try {
      String url = reviewServiceUrl + "?productId=" + productId;
      LOG.debug("Will call the deleteReviews API on URL: {}", url);
      restTemplate.delete(url);
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  private RuntimeException handleHttpClientException(HttpClientErrorException ex) {

    switch (ex.getStatusCode()) {
      case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
      case UNPROCESSABLE_ENTITY ->
          throw new InvalidInputException(getErrorMessage(ex));
      default -> {
        LOG.warn("Got an unexpected HTTP error: {}, will retrhow it", ex.getStatusCode());
        LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        throw ex;
      }
    }
  }

  private String getErrorMessage(HttpClientErrorException ex) {

    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}
