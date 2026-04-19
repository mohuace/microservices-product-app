package com.mohit.microservice.product_service.service;

import com.mohit.microservice.product_service.entity.Product;
import java.util.List;

public interface IProductService {
    List<Product> getAllProducts();
    Product getProductById(String productId);
    List<Product> getProductsByIds(List<String> productIds);
}
