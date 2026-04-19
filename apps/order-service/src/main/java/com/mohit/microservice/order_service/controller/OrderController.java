package com.mohit.microservice.order_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.mohit.microservice.order_service.model.ProductDetails;
import com.mohit.microservice.order_service.service.IOrderService;
import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /***
     * This will list all the orders for a particular user. (Currently not taking user id, assuming only one user)
     * @return
     */
    @GetMapping("/orders")
    public String getOrders() {
        return "Not yet implemented";
    }


    @PostMapping("/orders")
    public String createOrder(@RequestBody List<ProductDetails> products) {
        return orderService.createOrder(products);
    }

}
