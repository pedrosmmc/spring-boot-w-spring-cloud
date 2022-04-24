package com.pedrocoelho.microservices.core.product.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class ProductEntity {
  @Id
  private String id;
  /* INFO: The version field is used to implement optimistic locking, allowing Spring Data to verify that updates of an entity in the database do not overwrite a concurrent update. */
  @Version
  private Integer version;
  /* INFO: in order to work the unique, we must had this property: "spring.data.mongodb.auto-index-creation: true" to application properties file. */
  @Indexed(unique = true)
  private int productId;
  private String name;
  private int weight;

  public ProductEntity() {}

  public ProductEntity(int productId, String name, int weight) {
    this.productId = productId;
    this.name = name;
    this.weight = weight;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  // FOR DEBUG
  @Override
  public String toString() {
    return "ProductEntity{" +
        "id='" + id + '\'' +
        ", version=" + version +
        ", productId=" + productId +
        ", name='" + name + '\'' +
        ", weight=" + weight +
        '}';
  }
}
