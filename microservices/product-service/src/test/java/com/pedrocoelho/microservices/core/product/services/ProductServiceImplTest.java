package com.pedrocoelho.microservices.core.product.services;

import com.pedrocoelho.api.core.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceImplTest {

  @Autowired
  private WebTestClient client;

  private static final int PRODUCT_ID_OK = 123;
  private static final String productServiceUri = "/product/";

  @MockBean
  private ProductServiceImpl productService;

  @BeforeEach
  void setUp() {
    when(productService.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "product-name", 123, "mock-address"));
  }

  @Test
  void getProduct() {
    int productId = 123;

    client.get()
        .uri(productServiceUri + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK);
  }

  @Test
  void getProductById() {

    int productId = 1;

    client.get()
        .uri("/product/" + productId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.productId").isEqualTo(productId);
  }

  @Test
  void getProductInvalidParameterString() {

    client.get()
        .uri("/product/no-integer")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(BAD_REQUEST)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/no-integer")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
  }

  @Test
  void getProductNotFound() {

    int productIdNotFound = 13;

    client.get()
        .uri("/product/" + productIdNotFound)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
        .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
  }

  @Test
  void getProductInvalidParameterNegativeValue() {

    int productIdInvalid = -1;

    client.get()
        .uri("/product/" + productIdInvalid)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
        .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
  }
}