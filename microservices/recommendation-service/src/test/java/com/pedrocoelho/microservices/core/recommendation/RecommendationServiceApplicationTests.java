package com.pedrocoelho.microservices.core.recommendation;

import com.pedrocoelho.api.core.recommendation.Recommendation;
import com.pedrocoelho.microservices.core.recommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private RecommendationRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    assertEquals(0, repository.count());
  }

  @Test
  void getRecommendationsByProductId() {

    int productId = 1;
    int recommendationId = 1;

    postRecommendationAndVerify(productId, 1, OK);
    postRecommendationAndVerify(productId, 2, OK);
    postRecommendationAndVerify(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).size());

    this.getRecommendationsByProductIdAndVerify(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[2].productId").isEqualTo(productId)
        .jsonPath("$[2].recommendationId").isEqualTo(recommendationId + 2);
  }

  @Test
  void duplicateError() {

    int productId = 2;
    int recommendationId = 2;

    postRecommendationAndVerify(productId, recommendationId, OK)
        .jsonPath("$.productId").isEqualTo(productId)
        .jsonPath("$.recommendationId").isEqualTo(recommendationId);

    assertEquals(1, repository.count());

    postRecommendationAndVerify(productId, recommendationId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId + ", Recommendation Id: " + recommendationId);

    assertEquals(1, repository.count());
  }

  @Test
  void deleteRecommendations() {

    int productId = 1;
    int recommendationId = 1;

    postRecommendationAndVerify(productId, recommendationId, OK);
    assertEquals(1, repository.findByProductId(productId).size());

    deleteRecommendationAndVerify(productId, OK);
    assertEquals(0, repository.findByProductId(productId).size());

    deleteRecommendationAndVerify(productId, OK);
  }

  @Test
  void getRecommendationsMissingParameter() {

    getRecommendationsByProductIdAndVerify("", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
  }

  @Test
  void getRecommendationsInvalidParameter() {

    getRecommendationsByProductIdAndVerify("?productId=no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getRecommendationsNotFound() {

    int productIdNotFound = 113;

    getRecommendationsByProductIdAndVerify(productIdNotFound, OK)
        .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getRecommendationsInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    getRecommendationsByProductIdAndVerify("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/recommendation")
        .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  @Test
  void contextLoads() {

  }

  private WebTestClient.BodyContentSpec getRecommendationsByProductIdAndVerify(int productId, HttpStatus expectedStatus) {

    return getRecommendationsByProductIdAndVerify("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getRecommendationsByProductIdAndVerify(String productIdQuery, HttpStatus expectedStatus) {

    return client.get()
        .uri("/recommendation" + productIdQuery)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec postRecommendationAndVerify(int productId, int recommendationId, HttpStatus expectedStatus) {

    Recommendation recomendation = new Recommendation(productId, recommendationId, "Author-" + recommendationId, recommendationId, "Content-" + recommendationId, "SA");
    return client.post()
        .uri("/recommendation")
        .body(just(recomendation), Recommendation.class)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec deleteRecommendationAndVerify(int productId, HttpStatus expectedStatus) {

    return client.delete()
        .uri("/recommendation?productId=" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }
}
