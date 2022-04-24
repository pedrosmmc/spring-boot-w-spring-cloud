package com.pedrocoelho.api.core.product;

public class Product {

  private int productId;
  private String name;
  private int weight;
  private String serviceAddress;

  public Product() {

    productId = 0;
    name = null;
    weight = 0;
    serviceAddress = null;
  }

  public Product(int productId, String name, int weight, String serviceAddress) {

    this.productId = productId;
    this.name = name;
    this.weight = weight;
    this.serviceAddress = serviceAddress;
  }

  public int getProductId() {

    return productId;
  }

  public String getName() {

    return name;
  }

  public int getWeight() {

    return weight;
  }

  public String getServiceAddress() {

    return serviceAddress;
  }

  public Product setProductId(int productId) {

    this.productId = productId;
    return this;
  }

  public Product setName(String name) {

    this.name = name;
    return this;
  }

  public Product setWeight(int weight) {

    this.weight = weight;
    return this;
  }

  public Product setServiceAddress(String serviceAddress) {

    this.serviceAddress = serviceAddress;
    return this;
  }

  // FOR DEBUG
  @Override
  public String toString() {

    return "Product{" +
        "productId=" + productId +
        ", name='" + name + '\'' +
        ", weight=" + weight +
        ", serviceAddress='" + serviceAddress + '\'' +
        '}';
  }
}
