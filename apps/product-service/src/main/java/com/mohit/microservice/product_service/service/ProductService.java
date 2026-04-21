package com.mohit.microservice.product_service.service;

import com.mohit.microservice.product_service.entity.Product;
import com.mohit.microservice.product_service.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        log.info("Found {} products", products.size());
        return products;
    }

    @Override
    public Product getProductById(String productId) {
        log.info("Fetching product with id: {}", productId);
        try {
            Integer id = Integer.parseInt(productId);
            var product = productRepository.findById(id).orElse(null);
            if (product != null) {
                log.info("Product found with id: {}, name: {}", id, product.getName());
            } else {
                log.warn("Product not found with id: {}", id);
            }
            return product;
        } catch (NumberFormatException e) {
            log.error("Invalid product id format: {}", productId, e);
            return null;
        }
    }

    @Override
    public List<Product> getProductsByIds(List<String> productIds) {
        log.info("Fetching products for batch request with {} ids", productIds.size());
        //TODO: Can change later to accept integer ids.
        List<Integer> ids = productIds.stream()
                .map(id -> {
                    try {
                        return Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid product id format in batch: {}", id);
                        return null;
                    }
                })
                .filter(id -> id != null)
                .collect(Collectors.toList());

        log.info("Valid ids after filtering: {}", ids.size());
        List<Product> products = productRepository.findAllById(ids);
        log.info("Found {} products from batch request", products.size());
        return products;
    }
}
