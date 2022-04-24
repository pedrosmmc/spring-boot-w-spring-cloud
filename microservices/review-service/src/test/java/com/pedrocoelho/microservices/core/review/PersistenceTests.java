package com.pedrocoelho.microservices.core.review;

import com.pedrocoelho.microservices.core.review.repository.ReviewEntity;
import com.pedrocoelho.microservices.core.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;


@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
/* INFO: The @DataJpaTest annotation is designed to start an embedded database by default. Since we want to use a containerized database, we have to disable this feature. */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends MySqlTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenceTests.class);

  @Autowired
  private ReviewRepository repository;
  private ReviewEntity savedEntity;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
    savedEntity = repository.save(entity);
    assertEqualsReview(entity, savedEntity);
    assertEquals(1, repository.count());
  }


  @Test
  void create() {
    ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
    repository.save(newEntity);
    ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEqualsReview(foundEntity, newEntity);
    assertEquals(2, repository.count());
  }

  @Test
  void getByProductId() {
    List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());
    assertEqualsReview(entityList.get(0), savedEntity);
    assertThat(entityList, hasSize(1));
  }

  @Test
  void getByReviewId() {
    List<ReviewEntity> entityList = repository.findByReviewId(savedEntity.getReviewId());
    assertEqualsReview(entityList.get(0), savedEntity);
    assertThat(entityList, hasSize(1));
  }

  @Test
  void update() {
    savedEntity.setAuthor("a1");
    repository.save(savedEntity);
    ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(savedEntity.getVersion() + 1, foundEntity.getVersion());
    assertEquals(savedEntity.getAuthor(), foundEntity.getAuthor());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
    assertEquals(0, repository.count());
  }

  @Test
  void duplicateError() {
    ReviewEntity newEntity = new ReviewEntity(1, 2, "a", "s", "c");
    assertThrows(DataIntegrityViolationException.class, () -> {
      repository.save(newEntity);
    });
  }

  @Test
  void optimisticLockError() {
    ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
    ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setAuthor("a1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setAuthor("a2");
      repository.save(entity2);
    });

    ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(savedEntity.getVersion() + 1, updatedEntity.getVersion());
    assertEquals(entity1.getAuthor(), updatedEntity.getAuthor());
  }

  private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
    assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
    assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
    assertEquals(expectedEntity.getContent(), actualEntity.getContent());
  }
}
