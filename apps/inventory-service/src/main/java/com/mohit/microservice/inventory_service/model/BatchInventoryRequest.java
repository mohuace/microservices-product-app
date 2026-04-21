package com.mohit.microservice.inventory_service.model;

import lombok.Data;

import java.util.List;

@Data
public class BatchInventoryRequest {

    private List<String> productIds;

}

