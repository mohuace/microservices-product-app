package com.mohit.microservice.inventory_service.controller;

import com.mohit.microservice.inventory_service.model.InventoryUpdateRequest;
import com.mohit.microservice.inventory_service.service.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private IInventoryService inventoryService;

    @GetMapping
    public Object getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{productId}")
    public Object getInventoryByProductId(@PathVariable String productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @PostMapping("/{productId}/add")
    // Note: Add operations are not idempotent, hence using POST instead of PUT.
    public void addInventory(@PathVariable String productId, @RequestBody InventoryUpdateRequest request) {
        inventoryService.addInventory(productId, request);
    }

    @PostMapping("/{productId}/deduct")
    // Note: Deduct operations are not idempotent, hence using POST instead of PUT.
    public void deductInventory(@PathVariable String productId, @RequestBody InventoryUpdateRequest request) {
        inventoryService.deductInventory(productId, request);
    }

}
