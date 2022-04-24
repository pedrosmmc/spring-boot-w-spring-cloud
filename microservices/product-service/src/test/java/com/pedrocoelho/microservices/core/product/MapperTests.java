package com.pedrocoelho.microservices.core.product;

import com.pedrocoelho.api.core.product.Product;
import com.pedrocoelho.microservices.core.product.repository.ProductEntity;
import com.pedrocoelho.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

  private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Product model = new Product(1, "n", 1, "sa");

    ProductEntity entity = mapper.modelToEntity(model);

    assertEquals(model.getProductId(), entity.getProductId());
    assertEquals(model.getProductId(), entity.getProductId());
    assertEquals(model.getName(), entity.getName());
    assertEquals(model.getWeight(), entity.getWeight());

    Product model2 = mapper.entityToModel(entity);

    assertEquals(model.getProductId(), model2.getProductId());
    assertEquals(model.getProductId(), model2.getProductId());
    assertEquals(model.getName(), model2.getName());
    assertEquals(model.getWeight(), model2.getWeight());
    assertNull(model2.getServiceAddress());
  }
}
