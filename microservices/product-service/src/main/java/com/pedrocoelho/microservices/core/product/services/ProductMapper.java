package com.pedrocoelho.microservices.core.product.services;

import com.pedrocoelho.api.core.product.Product;
import com.pedrocoelho.microservices.core.product.repository.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mappings({
      @Mapping(target="serviceAddress", ignore = true)
  })
  Product entityToModel(ProductEntity entity);

  @Mappings({
      @Mapping(target="id",ignore = true),
      @Mapping(target = "version", ignore = true)
  })
  ProductEntity modelToEntity(Product product);
}
