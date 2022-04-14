package com.pedrocoelho.microservices.core.product.services;

import com.pedrocoelho.api.core.product.Product;
import com.pedrocoelho.api.core.product.ProductService;
import com.pedrocoelho.api.exceptions.InvalidInputException;
import com.pedrocoelho.api.exceptions.NotFoundException;
import com.pedrocoelho.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/* INFO: We create our service implementation in order to  implemente the Java interface from the api project and annotate the class with @RestController so that Spring will call the methods in this class according to the mappings specified in the Interface class. */
@RestController
public class ProductServiceImpl implements ProductService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  /* INFO: To enable ServiceUtil class from the util project, we inject it into the constructor. */
  private final ServiceUtil serviceUtil;

  public ProductServiceImpl(ServiceUtil serviceUtil) { this.serviceUtil = serviceUtil; }

  @Override
  public Product getProduct(int productId) {

      if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    /* INFO: Since we aren't currently using a database, we simply return a hardcoded response based on the input of productId, along with the service address supplied by the ServiceUtil. */
   List<Integer> availableProductsIds = new ArrayList<>(){{
     add(123);
     add(254);
     add(999);
   }};

    if(!availableProductsIds.contains(productId)) {
      LOG.debug("No product found for productId: {}", productId);
      throw new NotFoundException("Not found product with id: " + productId);
    }

    Product product =  new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());

    LOG.debug("/product response: {}", product);
    return product ;
  }
}
