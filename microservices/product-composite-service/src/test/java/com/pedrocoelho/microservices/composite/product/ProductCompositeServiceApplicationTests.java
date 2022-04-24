package com.pedrocoelho.microservices.composite.product;

import com.pedrocoelho.api.composite.product.ProductAggregate;
import com.pedrocoelho.api.composite.product.RecommendationSummary;
import com.pedrocoelho.api.composite.product.ReviewSummary;
import com.pedrocoelho.api.core.product.Product;
import com.pedrocoelho.api.core.recommendation.Recommendation;
import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.api.exceptions.InvalidInputException;
import com.pedrocoelho.api.exceptions.NotFoundException;
import com.pedrocoelho.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  private static final int PRODUCT_ID_OK = 123;
  private static final int PRODUCT_ID_NOT_FOUND = 13;
  private static final int PRODUCT_ID_INVALID = -1;

  @MockBean
  private ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {

    when(compositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "name", 123, "mock-address"));
    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 123, "author", 123, "content", "mock-address")));
    when(compositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(singletonList(new Review(PRODUCT_ID_OK, 123, "author", "subject", "content", "mock-address")));
    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("Not found product with id: " + PRODUCT_ID_NOT_FOUND));
    when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("Invalid product id: " + PRODUCT_ID_INVALID));
  }

  @Test
  @DisplayName("create a product without recommendations and reviews")
  void createCompositeProduct() {

    ProductAggregate compositeProduct = new ProductAggregate(PRODUCT_ID_OK, "author-" + PRODUCT_ID_OK, 100, null, null, null);

    postProductAndVerify(compositeProduct, OK);

    getProductByIdAndVerify(compositeProduct.getProductId(), OK);
  }

  @Test
  @DisplayName("create a product with recommendation and review")
  void createCompositeProduct2() {

    RecommendationSummary recommendationSummary = new RecommendationSummary(2, "author-" + PRODUCT_ID_OK, 3, "content-" + PRODUCT_ID_OK);
    ReviewSummary reviewSummary = new ReviewSummary(3, "author-" + PRODUCT_ID_OK, "subject-" + PRODUCT_ID_OK, "content-" + PRODUCT_ID_OK);
    ProductAggregate compositeProduct = new ProductAggregate(PRODUCT_ID_OK, "author-" + PRODUCT_ID_OK, 100, singletonList(recommendationSummary), singletonList(reviewSummary), null);

    postProductAndVerify(compositeProduct, OK);

    getProductByIdAndVerify(compositeProduct.getProductId(), OK);
  }

  @Test
  @DisplayName("create a duplicated product")
  void createProductByIdDuplicated() {
    //TODO?
  }

  @Test
  @DisplayName("get a product with valid and existing id")
  void getProductById() {

    getProductByIdAndVerify(PRODUCT_ID_OK, OK)
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  @DisplayName("get a product with valid but non-existing id")
  void getProductNotFound() {

    getProductByIdAndVerify(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("Not found product with id: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  @DisplayName("get a product with invalid id")
  void getProductInvalidIdParameterNegativeValue() {

    getProductByIdAndVerify(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
        .jsonPath("$.message").isEqualTo("Invalid product id: " + PRODUCT_ID_INVALID);
  }

  @Test
  @DisplayName("delete a product by id")
  void deleteCompositeProduct() {

    RecommendationSummary recommendationSummary = new RecommendationSummary(2, "author-" + PRODUCT_ID_OK, 3, "content-" + PRODUCT_ID_OK);
    ReviewSummary reviewSummary = new ReviewSummary(3, "author-" + PRODUCT_ID_OK, "subject-" + PRODUCT_ID_OK, "content-" + PRODUCT_ID_OK);
    ProductAggregate compositeProduct = new ProductAggregate(PRODUCT_ID_OK, "author-" + PRODUCT_ID_OK, 100, singletonList(recommendationSummary), singletonList(reviewSummary), null);

    postProductAndVerify(compositeProduct, OK);
    deleteProductByIdAndVerify(compositeProduct.getProductId(), OK);
    deleteProductByIdAndVerify(compositeProduct.getProductId(), OK);
  }

  private void postProductAndVerify(ProductAggregate compositeProduct, HttpStatus expectedStatus) {

    client.post()
        .uri("/product-composite")
        .accept(APPLICATION_JSON)
        .body(just(compositeProduct), ProductAggregate.class)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus);
  }

  private WebTestClient.BodyContentSpec getProductByIdAndVerify(int productId, HttpStatus expectedStatus) {

    return client.get()
        .uri("/product-composite/" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private void deleteProductByIdAndVerify(int productId, HttpStatus expectedStatus) {

    client.delete()
        .uri("/product-composite/" + productId)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus);
  }

  @Test
  void contextLoads() {

  }
}
