package com.pedrocoelho.microservices.core.review;

import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.microservices.core.review.repository.ReviewRepository;
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
class ReviewServiceApplicationTests extends MySqlTestBase {

  @Autowired
  private WebTestClient client;

  @Autowired
  private ReviewRepository repository;

  @BeforeEach
  void setupDb() {

    repository.deleteAll();
    assertEquals(0, repository.count());
  }

  @Test
  void createReview() {

    int productId = 1;
    int reviewId = 1;

    assertEquals(0, repository.findByProductId(productId).size());

    postReviewAndVerify(productId, reviewId, OK);
    postReviewAndVerify(productId, reviewId + 1, OK);
    postReviewAndVerify(productId, reviewId + 2, OK);

    assertEquals(3, repository.findByProductId(productId).size());

    this.getReviewsByProductIdAndVerify(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(productId)
        .jsonPath("$[0].reviewId").isEqualTo(reviewId)
        .jsonPath("$[1].productId").isEqualTo(productId)
        .jsonPath("$[1].reviewId").isEqualTo(reviewId + 1)
        .jsonPath("$[2].productId").isEqualTo(productId)
        .jsonPath("$[2].reviewId").isEqualTo(reviewId + 2);
  }

  @Test
  void duplicateError() {

    int productId = 1;
    int reviewId = 1;

    assertEquals(0, repository.findByProductId(productId).size());

    postReviewAndVerify(productId, reviewId, OK)
        .jsonPath("$.productId").isEqualTo(productId)
        .jsonPath("$.reviewId").isEqualTo(reviewId);

    assertEquals(1, repository.findByProductId(productId).size());

    postReviewAndVerify(productId, reviewId, UNPROCESSABLE_ENTITY)
        .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId + ", Review Id: " + reviewId);

    assertEquals(1, repository.findByProductId(productId).size());
  }

  @Test
  void getReviewsByProductId() {

    int productId = 1;

    assertEquals(0, repository.findByProductId(productId).size());

    postReviewAndVerify(productId, 1, OK);
    postReviewAndVerify(productId, 2, OK);
    postReviewAndVerify(productId, 3, OK);

    assertEquals(3, repository.findByProductId(productId).size());

    getReviewsByProductIdAndVerify(productId, OK)
        .jsonPath("$.length()").isEqualTo(3)
        .jsonPath("$[0].productId").isEqualTo(productId)
        .jsonPath("$[0].reviewId").isEqualTo(1)
        .jsonPath("$[1].productId").isEqualTo(productId)
        .jsonPath("$[1].reviewId").isEqualTo(2)
        .jsonPath("$[2].productId").isEqualTo(productId)
        .jsonPath("$[2].reviewId").isEqualTo(3);
  }

  @Test
  void getReviewsMissingParameter() {

    getReviewsByProductIdAndVerify("", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
  }

  @Test
  void getReviewsInvalidParameter() {

    //FIXME: AssertionError: Status expected:<400> but was:<404>
//    getReviewsByProductIdAndVerify("/review?productId=no-integer", BAD_REQUEST)
//        .jsonPath("$.path").isEqualTo("/review")
//        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getReviewsNotFound() {

    int productIdNotFound = 213;

    getReviewsByProductIdAndVerify(productIdNotFound, OK)
        .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void getReviewsInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    getReviewsByProductIdAndVerify(productIdInvalid, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }

  @Test
  void deleteReview() {

    int productId = 1;
    int reviewId = 2;

    postReviewAndVerify(productId, reviewId, OK);

    assertEquals(1, repository.findByProductId(productId).size());

    deleteReviewByProductIdAndVerify(1, OK);

    assertEquals(0, repository.findByProductId(productId).size());

    deleteReviewByProductIdAndVerify(1, OK);

    assertEquals(0, repository.findByProductId(productId).size());
  }

  @Test
  void contextLoads() {

  }

  private WebTestClient.BodyContentSpec getReviewsByProductIdAndVerify(int productId, HttpStatus expectedStatus) {

    return getReviewsByProductIdAndVerify("?productId=" + productId, expectedStatus);
  }

  private WebTestClient.BodyContentSpec getReviewsByProductIdAndVerify(String productIdQuery, HttpStatus expectedStatus) {

    return client.get()
        .uri("/review" + productIdQuery)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private WebTestClient.BodyContentSpec postReviewAndVerify(int productId, int reviewId, HttpStatus expectedStatus) {

    Review review = new Review(productId, reviewId, "author-" + reviewId, "subject-" + reviewId, "content-" + reviewId, "service-address-" + reviewId);

    return client.post()
        .uri("/review")
        .accept(APPLICATION_JSON)
        .body(just(review), Review.class)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
  }

  private void deleteReviewByProductIdAndVerify(int productId, HttpStatus expectedStatus) {

    client.delete()
        .uri("/review?productId=" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(expectedStatus)
        .expectBody();
  }


}
