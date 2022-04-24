package com.pedrocoelho.microservices.core.product;

import com.pedrocoelho.microservices.core.product.MongoDbTestBase;
import com.pedrocoelho.microservices.core.product.repository.ProductEntity;
import com.pedrocoelho.microservices.core.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

/* INFO: The @DataMongoTest annotation is designed to start an embedded database by default. Since we want to use a containerized database, we have to disable this feature. */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private ProductRepository repository;
  private ProductEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
    ProductEntity entity = new ProductEntity(1, "n", 1);
    savedEntity = repository.save(entity);
    assertEqualsProduct(entity, savedEntity);
  }

  @Test
  void create() {
    ProductEntity newEntity = new ProductEntity(2, "n", 2);
    repository.save(newEntity);
    ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEqualsProduct(newEntity, foundEntity);
    assertEquals(2, repository.count());
  }

  @Test
  void update() {
    savedEntity.setName("n2");
    repository.save(savedEntity);
    ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long) foundEntity.getVersion());
    assertEquals(savedEntity.getName(), foundEntity.getName());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void getByProductId() {
    Optional<ProductEntity> entity = repository.findById(savedEntity.getId());
    assertTrue(entity.isPresent());
    assertEqualsProduct(savedEntity, entity.get());
  }

  /**
   * The test tries to store an entity with the same business key as used by the entity created by the setup method. The test will fail if the save operation succeeds, or if the save fails with an exception other than the expected DuplicateKeyException.
   */
  @Test
  void duplicateError() {
    assertThrows(DuplicateKeyException.class, () -> {
      ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
      repository.save(entity);
    });
  }

  /**
   * It is a test that verifies a correct error handling in the case of updates of stale data — it verifies that the optimistic locking mechanism works.
   */
  /* INFO: First, the test reads the same entity twice and stores it in two different variables, entity1 and entity2.
      Next, it uses one of the variables, entity1, to update the entity. The update of the entity in the database will cause the version field of the entity to be increased automatically by Spring Data. The other variable, entity2, now contains stale data, manifested by its version field, which holds a lower value than the corresponding value in the database.
      When the test tries to update the entity using the variable entity2, which contains stale data, it is expected to fail by throwing an OptimisticLockingFailureException exception. The test wraps up by asserting that the entity in the database reflects the first update, that is, contains the name "n1", and that the version field has the value 1; only one update has been performed on the entity in the database. */
  @Test
  void optimisticLockError() {
    ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
    ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setName("n1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setName("n2");
      repository.save(entity2);
    });

    ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(entity1.getVersion(), (int) updatedEntity.getVersion());
    assertEquals(entity1.getName(), updatedEntity.getName());
  }

  @Test
  void paging() {
    repository.deleteAll();

    List<ProductEntity> newProducts = rangeClosed(1001, 1010)
        .mapToObj(i -> new ProductEntity(i, "name-" + i, i))
        .collect(Collectors.toList());
    repository.saveAll(newProducts);

    Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
    nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
    nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
    testNextPage(nextPage, "[1009, 1010]", false);
  }

  private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
    Page<ProductEntity> productPage = repository.findAll(nextPage);
    assertEquals(expectedProductIds, productPage.getContent().stream().map(p->p.getProductId()).collect(Collectors.toList()).toString());
    return productPage.nextPageable();
  }

  private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getProductId(), expectedEntity.getProductId());
    assertEquals(expectedEntity.getName(), actualEntity.getName());
    assertEquals(expectedEntity.getWeight(), actualEntity.getWeight());
  }
}
