package com.mohit.microservice.product_service.controller;

import com.mohit.microservice.product_service.entity.Product;
import com.mohit.microservice.product_service.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable String productId) {
        return productService.getProductById(productId);
    }

    @PostMapping("/batch")
    public List<Product> getProductsByIds(@RequestBody List<String> productIds) {
        return productService.getProductsByIds(productIds);
    }
}
