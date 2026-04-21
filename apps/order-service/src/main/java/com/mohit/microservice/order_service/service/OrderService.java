package com.mohit.microservice.order_service.service;

import com.mohit.microservice.order_service.model.ProductDetails;
import com.mohit.microservice.order_service.model.Product;
import com.mohit.microservice.order_service.model.Inventory;
import com.mohit.microservice.order_service.entity.Order;
import com.mohit.microservice.order_service.entity.OrderItem;
import com.mohit.microservice.order_service.entity.Status;
import com.mohit.microservice.order_service.repository.OrderRepository;
import com.mohit.microservice.order_service.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String createOrder(List<ProductDetails> products) {
        Integer orderId = null;
        boolean isOrderCreatedSuccessfully = false;
        try {
            orderId = initiateOrderCreation(products);
            isOrderCreatedSuccessfully = true;
            return "Order created with ID: " + orderId;
        } catch (Exception e) {
            handleError(orderId, e);
            return "Order creation failed";
        } finally {
            if (isOrderCreatedSuccessfully && orderId != null) {
                updateInventoryAfterOrderSuccess(products);
            }
        }
    }

    private Integer initiateOrderCreation(List<ProductDetails> products) {
        // 1. Create an entry in order table with status as PENDING
        Integer orderId = createOrderEntry();

        // 2. Get the product details from product service
        List<Product> productList = getProductDetailsFromService(products);

        // 3. Check the inventory service for the availability
        checkInventoryAvailability(products);

        // 4. Calculate total price of the order.
        BigDecimal totalPrice = calculateTotalPrice(productList, products);

        // 5. Make the payment, call payment service.
        // TODO: Implement payment. TBD Later

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

    private void updateOrderWithItems(Integer orderId, List<ProductDetails> products, BigDecimal totalPrice) {
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
        order.setTotalPrice(totalPrice);
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

    private BigDecimal calculateTotalPrice(List<Product> productList, List<ProductDetails> products) {
        // Create a map for quick lookup of products by ID
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (ProductDetails productDetails : products) {
            Product product = productMap.get(Integer.parseInt(productDetails.getProductId()));
            if (product != null) {
                totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(productDetails.getQuantity())));
            }
        }
        return totalPrice;
    }

    private List<Product> getProductDetailsFromService(List<ProductDetails> products) {
        List<String> productIds = products.stream().map(ProductDetails::getProductId).collect(Collectors.toList());
        String ids = String.join(",", productIds);
        ServiceInstance productService = discoveryClient.getInstances("product-service").get(0);
        String url = productService.getUri() + "/products/batch?ids=" + ids;
        ResponseEntity<List<Product>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {});
        return response.getBody();
    }

    private void checkInventoryAvailability(List<ProductDetails> products) {
        List<Inventory> inventoryList = getInventoryDetailsFromService(products);
        Map<Integer, Inventory> inventoryMap = buildInventoryMap(inventoryList);
        validateInventoryForProducts(products, inventoryMap);
    }

    private List<Inventory> getInventoryDetailsFromService(List<ProductDetails> products) {
        List<String> productIds = products.stream().map(ProductDetails::getProductId).collect(Collectors.toList());
        String ids = String.join(",", productIds);
        ServiceInstance inventoryService = discoveryClient.getInstances("inventory-service").get(0);
        String url = inventoryService.getUri() + "/inventory/batch?ids=" + ids;
        ResponseEntity<List<Inventory>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Inventory>>() {});
        return response.getBody();
    }

    private Map<Integer, Inventory> buildInventoryMap(List<Inventory> inventoryList) {
        return inventoryList.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));
    }

    private void validateInventoryForProducts(List<ProductDetails> products, Map<Integer, Inventory> inventoryMap) {
        for (ProductDetails productDetails : products) {
            validateProductInventory(productDetails, inventoryMap);
        }
    }

    private void validateProductInventory(ProductDetails productDetails, Map<Integer, Inventory> inventoryMap) {
        int productId = Integer.parseInt(productDetails.getProductId());
        int requestedQuantity = productDetails.getQuantity();
        Inventory inventory = inventoryMap.get(productId);

        if (inventory == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found in inventory");
        }

        int availableStock = inventory.getQuantity();
        if (availableStock - requestedQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock for product ID " + productId +
                ". Available: " + availableStock + ", Requested: " + requestedQuantity);
        }
    }

    private void updateInventoryAfterOrderSuccess(List<ProductDetails> products) {
        try {
            for (ProductDetails product : products) {
                deductProductInventory(product);
            }
            logger.info("Inventory updated successfully for order");
        } catch (Exception e) {
            logger.error("Error updating inventory after order creation", e);
        }
    }

    private void deductProductInventory(ProductDetails product) {
        int productId = Integer.parseInt(product.getProductId());
        int quantity = product.getQuantity();

        ServiceInstance inventoryService = discoveryClient.getInstances("inventory-service").get(0);
        String url = inventoryService.getUri() + "/inventory/" + productId + "/deduct";

        // Create request body with quantity
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("quantity", quantity);
        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(requestBody);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            logger.info("Inventory deducted for product ID: " + productId + ", Quantity: " + quantity);
        } catch (Exception e) {
            logger.error("Error deducting inventory for product ID: " + productId, e);
        }
    }
}
