package com.mohit.microservice.order_service.service;

import com.mohit.microservice.order_service.model.ProductDetails;
import java.util.List;

public interface IOrderService {
    String createOrder(List<ProductDetails> products);
}

