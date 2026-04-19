package com.mohit.microservice.inventory_service.service;

import com.mohit.microservice.inventory_service.entity.Inventory;
import com.mohit.microservice.inventory_service.exception.InsufficientInventoryException;
import com.mohit.microservice.inventory_service.model.InventoryUpdateRequest;
import com.mohit.microservice.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService implements IInventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public Inventory getInventoryByProductId(String productId) {
        Optional<Inventory> inventory = inventoryRepository.findById(Long.parseLong(productId));
        return inventory.orElse(null);
    }

    @Override
    public void addInventory(String productId, InventoryUpdateRequest request) {
        Optional<Inventory> existingInventory = inventoryRepository.findById(Long.parseLong(productId));
        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deductInventory(String productId, InventoryUpdateRequest request) {
        Optional<Inventory> existingInventory = inventoryRepository.findById(Long.parseLong(productId));
        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            int newQuantity = inventory.getQuantity() - request.getQuantity();
            if (newQuantity < 0) {
                throw new InsufficientInventoryException(
                    "Insufficient inventory for product ID " + productId +
                    ". Current quantity: " + inventory.getQuantity() +
                    ", Requested deduction: " + request.getQuantity()
                );
            }
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
        }
    }

}
