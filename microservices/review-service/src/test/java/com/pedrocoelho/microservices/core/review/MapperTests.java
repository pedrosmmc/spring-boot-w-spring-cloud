package com.pedrocoelho.microservices.core.review;

import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.microservices.core.review.repository.ReviewEntity;
import com.pedrocoelho.microservices.core.review.services.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MapperTests {

  private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Review model = new Review(1, 2, "a", "s", "c", "s");
    ReviewEntity entity = mapper.modelToEntity(model);

    assertEqualsReview(model, entity);

    Review model2 = mapper.entityToModel(entity);

    assertEqualsReview(model2, entity);

  }

  @Test
  void mapperListTest() {

    assertNotNull(mapper);

    List<Review> modelList = rangeClosed(0,10).mapToObj(i->new Review(i,i+1,"a"+i, "s"+i,"c"+i, "s"+i)).toList();
    List<ReviewEntity> entityList = mapper.modelListToEntityList(modelList);

    assertEquals(modelList.size(), entityList.size());
    rangeClosed(0,10).forEach(i->assertEqualsReview(modelList.get(i), entityList.get(i)));

    List<Review> modelList2 = mapper.entityListToModelList(entityList);

    rangeClosed(0,10).forEach(i->assertEqualsReview(modelList2.get(i), entityList.get(i)));
  }

  private void assertEqualsReview(Review model, ReviewEntity entity) {

    assertEquals(entity.getProductId(), model.getProductId());
    assertEquals(entity.getReviewId(), model.getReviewId());
    assertEquals(entity.getAuthor(), model.getAuthor());
    assertEquals(entity.getSubject(), model.getSubject());
    assertEquals(entity.getContent(), model.getContent());
  }
}
