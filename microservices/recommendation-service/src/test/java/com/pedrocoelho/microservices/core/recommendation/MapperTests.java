package com.pedrocoelho.microservices.core.recommendation;

import com.pedrocoelho.api.core.recommendation.Recommendation;
import com.pedrocoelho.microservices.core.recommendation.repository.RecommendationEntity;
import com.pedrocoelho.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;

public class MapperTests {

  private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Recommendation model = new Recommendation(1, 2, "a", 3, "c", "s");
    RecommendationEntity entity = mapper.modelToEntity(model);

    assertEquals(entity.getProductId(), model.getProductId());
    assertEquals(entity.getRecommendationId(), model.getRecommendationId());
    assertEquals(entity.getAuthor(), model.getAuthor());
    assertEquals(entity.getRating(), model.getRate());
    assertEquals(entity.getContent(), model.getContent());

    Recommendation model2 = mapper.entityToModel(entity);

    assertEquals(entity.getProductId(), model2.getProductId());
    assertEquals(entity.getRecommendationId(), model2.getRecommendationId());
    assertEquals(entity.getAuthor(), model2.getAuthor());
    assertEquals(entity.getRating(), model2.getRate());
    assertEquals(entity.getContent(), model2.getContent());
    assertNull(model2.getServiceAddress());

  }

  @Test
  void mapperListTests() {

    assertNotNull(mapper);

    List<Recommendation> modelList = rangeClosed(1000, 1010)
        .mapToObj(i -> new Recommendation(i, i + 1, "a-" + i, i + 2, "c-" + i, "s-"+1))
        .collect(Collectors.toList());
    List<RecommendationEntity> entityList = mapper.modelListToEntityList(modelList);

    assertEquals(modelList.size(), entityList.size());
    rangeClosed(0,10).forEach(i->assertEqualsRecommendation(entityList.get(i), modelList.get(i)));

    List<Recommendation> modelList2 = mapper.entityListToModelList(entityList);

    rangeClosed(0,10).forEach(i->assertEqualsRecommendation(entityList.get(i), modelList.get(i)));
  }

  private void assertEqualsRecommendation(RecommendationEntity entity, Recommendation model) {

    assertEquals(model.getProductId(), entity.getProductId());
    assertEquals(model.getRecommendationId(), entity.getRecommendationId());
    assertEquals(model.getAuthor(), entity.getAuthor());
    assertEquals(model.getRate(), entity.getRating());
    assertEquals(model.getContent(), entity.getContent());
  }
}
