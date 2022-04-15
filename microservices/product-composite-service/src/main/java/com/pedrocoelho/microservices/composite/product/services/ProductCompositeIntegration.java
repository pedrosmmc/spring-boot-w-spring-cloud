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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review/?productId=";
    }

    /* INFO: This method can be used in RestTemplate. The expected response is a Product object, and it can be expressed in the call to getForObject() by specifying the Product.class class that RestTemplate will map the JSON response to. */
    @Override
    public Product getProduct(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.getProductId());
            return product;
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        }
    }

    /* INFO: For the calls to getRecommendations() and getReviews(), a more advanced method, exchange(), has to be used. The reason for this is the automatic mapping from a JSON response to a model class that RestTemplate performs. */

    /* INFO: The getRecommendations() and getReviews() methods expect a generic list in the responses, that is, List<Recommendation> and List<Review>. Since generics don't hold any type of information at runtime, we can't specify that the methods expect a generic list in their responses. Instead, we can use a helper class from the Spring Framework, ParameterizedTypeReference, that is designed to resolve this problem by holding the type information at runtime. This means that RestTemplate can figure out what class to map the JSON responses to. To utilize this helper class, we have to use the more involved exchange() method instead of the simpler getForObject() method on RestTemplate. */

    /* INFO: The error handling for getRecommendations() and getReviews() in the integration component is a bit more relaxed – classed as best-effort, meaning that, if it succeeds in getting product information but fails to get either recommendations or reviews, it is still considered to be okay. However, a warning is written to the log. */

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            return restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() { }).getBody();
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            return restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() { }).getBody();
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will retrhow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
                }
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
