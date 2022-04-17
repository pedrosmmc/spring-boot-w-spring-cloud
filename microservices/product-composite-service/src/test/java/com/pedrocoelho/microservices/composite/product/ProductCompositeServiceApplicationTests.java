package com.pedrocoelho.microservices.composite.product;

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
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  private static final int PRODUCT_ID_OK = 123;
  private static final int PRODUCT_ID_NOT_FOUND = 13;
  private static final int PRODUCT_ID_INVALID = 1;
  private static final String PRODUCT_ID_BAD_REQUEST = "abc";
  private static final String productCompositeServiceUri = "/product-composite/";

  @MockBean
  private ProductCompositeIntegration productCompositeIntegration;

  @BeforeEach
  void setUp() {
    when(productCompositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "name", 123, "mock-address"));
    when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 123, "author", 123, "content", "mock-address")));
    when(productCompositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(singletonList(new Review(PRODUCT_ID_OK, 123, "author", "subject", "content", "mock-address")));
    when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("Not found product with id: " + PRODUCT_ID_NOT_FOUND));
    when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("Invalid product id: " + PRODUCT_ID_INVALID));
  }

  @Test
  void contextLoads() {
  }

  /* INFO: This teste consistes in sending in productId for an existing product and asserting that we get back 200 as an HTTP response code and a JSON response that contains the requested productId along with one recommendation and one review. */
  @Test
  @DisplayName("get a product with valid and existing id")
  public void getProductById() {
    int productId = 123;

    /* INFO: The test uses the fluent WebTestClient API to set up the URL to call "/product-composite/" + PRODUCT_ID_OK and specify the accepted response format, JSON. */
    client.get()
        .uri(productCompositeServiceUri + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        /* INFO: After executing the request using the exchange() method, the test verifies that the response status is OK (200) and that the response format actually is JSON (as requested). */
        .expectStatus().isOk()
        .expectHeader().contentType(APPLICATION_JSON)
        /* INFO: Finally, the test inspects the response body and verifies that it contains the expected information in terms of productId and the number of recommendations and reviews. */
        .expectBody()
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
        .jsonPath("$.recommendations.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  /* INFO: This negative test is verifies that it got an error status code back, Not Found (404), and that the response body contains the expected error message. */
  @Test
  @DisplayName("get a product with valid but non-existing id")
  public void getProductNotFound() {
    int productId = 13;

    client.get()
        .uri(productCompositeServiceUri + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo(productCompositeServiceUri + PRODUCT_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("Not found product with id: " + PRODUCT_ID_NOT_FOUND);
  }

  /* INFO: This negative test verifies that it got an error status code back, UNPROCESSABLE ENTITY (422), and that the response body contains the expected error message. */
  @Test
  @DisplayName("get a product with invalid id")
  public void getProductInvalidId() {
    int productId = 1;

    client.get()
        .uri(productCompositeServiceUri + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo(productCompositeServiceUri + PRODUCT_ID_INVALID)
        .jsonPath("$.message").isEqualTo("Invalid product id: " + PRODUCT_ID_INVALID);
  }

  /* INFO: This negative test verifies that it got an error status code back, BAD REQUEST (400), and that the response body contains the expected error message. */
  @Test
  @DisplayName("get a product with a id with letters")
  public void getProductBadRequest() {
    String productId = "abc";

    client.get()
        .uri(productCompositeServiceUri + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo(productCompositeServiceUri + PRODUCT_ID_BAD_REQUEST)
        .jsonPath("$.message").isEqualTo("Invalid product id : " + PRODUCT_ID_INVALID);
    ;
  }
}
