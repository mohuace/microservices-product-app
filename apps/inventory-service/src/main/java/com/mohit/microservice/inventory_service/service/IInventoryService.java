package com.mohit.microservice.inventory_service.service;

import com.mohit.microservice.inventory_service.entity.Inventory;
import com.mohit.microservice.inventory_service.model.InventoryUpdateRequest;

import java.util.List;

public interface IInventoryService {

    List<Inventory> getAllInventory();

    Inventory getInventoryByProductId(String productId);

    void addInventory(String productId, InventoryUpdateRequest request);

    void deductInventory(String productId, InventoryUpdateRequest request);

    List<Inventory> getBatchInventory(List<String> productIds);

}
