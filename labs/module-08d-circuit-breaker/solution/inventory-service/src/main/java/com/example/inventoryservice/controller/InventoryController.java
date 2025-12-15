package com.example.inventoryservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final Random random = new Random();
    private boolean simulateFailure = false;
    private boolean simulateSlow = false;

    @GetMapping("/{productId}")
    public Map<String, Object> getInventory(@PathVariable String productId) throws InterruptedException {
        if (simulateFailure) {
            throw new RuntimeException("Inventory service is down!");
        }

        if (simulateSlow) {
            Thread.sleep(5000);
        }

        Map<String, Object> inventory = new HashMap<>();
        inventory.put("productId", productId);
        inventory.put("quantity", random.nextInt(100));
        inventory.put("warehouse", "WAREHOUSE-A");
        inventory.put("status", "AVAILABLE");
        return inventory;
    }

    @PostMapping("/simulate/failure/{enable}")
    public Map<String, String> toggleFailure(@PathVariable boolean enable) {
        this.simulateFailure = enable;
        Map<String, String> response = new HashMap<>();
        response.put("failureSimulation", enable ? "enabled" : "disabled");
        return response;
    }

    @PostMapping("/simulate/slow/{enable}")
    public Map<String, String> toggleSlow(@PathVariable boolean enable) {
        this.simulateSlow = enable;
        Map<String, String> response = new HashMap<>();
        response.put("slowSimulation", enable ? "enabled" : "disabled");
        return response;
    }
}
