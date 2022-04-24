package com.pedrocoelho.microservices.core.review.services;

import com.pedrocoelho.api.core.review.Review;
import com.pedrocoelho.microservices.core.review.repository.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mappings({
      @Mapping(target = "serviceAddress", ignore = true)
  })
  Review entityToModel(ReviewEntity entity);

  @Mappings({
      @Mapping(target = "id", ignore = true),
      @Mapping(target = "version", ignore = true)
  })
  ReviewEntity modelToEntity(Review api);

  List<Review> entityListToModelList(List<ReviewEntity> entity);

  List<ReviewEntity> modelListToEntityList(List<Review> api);
}
