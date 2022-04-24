package com.pedrocoelho.microservices.core.recommendation;

import com.pedrocoelho.microservices.core.recommendation.repository.RecommendationEntity;
import com.pedrocoelho.microservices.core.recommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

/* INFO: The @DataMongoTest annotation is designed to start an embedded database by default. Since we want to use a containerized database, we have to disable this feature. */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private RecommendationRepository repository;
  private RecommendationEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
    RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
    savedEntity = repository.save(entity);
    assertEqualsRecommendation(entity, savedEntity);
  }

  @Test
  void create() {
    RecommendationEntity newEntity = new RecommendationEntity(1,3,"a",5,"c");
    repository.save(newEntity);
    assertEqualsRecommendation(newEntity, repository.findById(newEntity.getId()).get());
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    savedEntity.setContent("c2");
    repository.save(savedEntity);
    RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long) foundEntity.getVersion());
    assertEquals(savedEntity.getAuthor(), foundEntity.getAuthor());
    assertEquals(savedEntity.getContent(), foundEntity.getContent());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void getRecommendationByProductId() {
    List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());
    assertThat(entityList, hasSize(1));
    assertEqualsRecommendation(savedEntity, entityList.get(0));
  }

  @Test
  void getRecommendationByRecommendationId() {
    List<RecommendationEntity> entityList = repository.findByRecommendationId(savedEntity.getRecommendationId());
    assertThat(entityList, hasSize(1));
    assertEqualsRecommendation(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    RecommendationEntity entity = new RecommendationEntity(1,2,"a",3,"c");

    assertThrows(DuplicateKeyException.class, () -> {
      repository.save(entity);
    });
  }

  @Test
  void optimisticLockError() {
    RecommendationEntity recommendation1 = repository.findById(savedEntity.getId()).get();
    RecommendationEntity recommendation2 = repository.findById(savedEntity.getId()).get();

    recommendation1.setAuthor("a1");
    repository.save(recommendation1);

    assertThrows(OptimisticLockingFailureException.class, () ->{
      recommendation2.setAuthor("a2");
      repository.save(recommendation2);
    });

    RecommendationEntity updatedRecommendation = repository.findById(recommendation1.getId()).get();
    assertEquals(1, updatedRecommendation.getVersion());
    assertEquals(recommendation1.getAuthor(), updatedRecommendation.getAuthor());
  }

  private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
    assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
    assertEquals(expectedEntity.getRating(), actualEntity.getRating());
    assertEquals(expectedEntity.getContent(), actualEntity.getContent());
  }
}
