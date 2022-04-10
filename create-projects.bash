#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.6.6.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-service \
--package-name=com.pedrocoelho.microservices.core.product \
--groupId=com.pedrocoelho.microservices.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-service

spring init \
--boot-version=2.6.6.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=review-service \
--package-name=com.pedrocoelho.microservices.core.review \
--groupId=com.pedrocoelho.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.6.6.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=recommendation-service \
--package-name=com.pedrocoelho.microservices.core.recommendation \
--groupId=com.pedrocoelho.microservices.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommendation-service

spring init \
--boot-version=2.6.6.RELEASE \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-composite-service \
--package-name=com.pedrocoelho.microservices.composite.product \
--groupId=com.pedrocoelho.microservices.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
product-composite-service

cd ..
