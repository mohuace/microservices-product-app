package com.mohit.microservice.order_service.service;

import com.mohit.microservice.order_service.model.ProductDetails;
import com.mohit.microservice.order_service.entity.Order;
import com.mohit.microservice.order_service.entity.OrderItem;
import com.mohit.microservice.order_service.entity.Status;
import com.mohit.microservice.order_service.repository.OrderRepository;
import com.mohit.microservice.order_service.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Service
public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public String createOrder(List<ProductDetails> products) {
        Integer orderId = null;
        try {
            orderId = initiateOrderCreation(products);
            return "Order created with ID: " + orderId;
        } catch (Exception e) {
            handleError(orderId, e);
            return "Order creation failed";
        } finally {
            // TODO: Update inventory once all steps are successful. Call inventory service.
        }
    }

    private Integer initiateOrderCreation(List<ProductDetails> products) {
        // 1. Create an entry in order table with status as PENDING
        Integer orderId = createOrderEntry();

        //TODO: 2. Get the product details from product service
        // 3. Check the inventory service for the availability
        // 4. Calculate total price of the order.
        // For now dummy price of the order.
        Integer totalPrice = 1500;
        // 5. Make the payment, call payment service.
        // Update the db
        updateOrderWithItems(orderId, products, totalPrice);
        return orderId;
    }

    private Integer createOrderEntry() {
        Order order = new Order();
        order.setStatus(Status.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);
        Order savedOrder = orderRepository.save(order);
        return savedOrder.getId();
    }

    private void updateOrderWithItems(Integer orderId, List<ProductDetails> products, Integer totalPrice) {
        // Fetch the order
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return;
        }

        // Create OrderItem entries from products
        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductDetails product : products) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(Integer.parseInt(product.getProductId()));
            item.setQuantity(product.getQuantity());
            orderItems.add(item);
        }

        // Save all order items
        //orderItemRepository.saveAll(orderItems);

        // Update order status to COMPLETED and set total price
        order.setStatus(Status.COMPLETED);
        order.setTotalPrice(BigDecimal.valueOf(totalPrice));
        order.setOrderItems(orderItems);
        orderRepository.save(order);
    }

    private void handleError(Integer orderId, Exception e) {
        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus(Status.FAILED);
                orderRepository.save(order);
            }
        }
        logger.error("Error creating order with ID: " + orderId, e);
    }
}
