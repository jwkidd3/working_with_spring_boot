package com.example.orderservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final Map<Long, Map<String, Object>> orders = new HashMap<>();
    private long nextId = 1;

    public OrderController() {
        addOrder(1L, "Laptop", 999.99);
        addOrder(2L, "Phone", 599.99);
    }

    private void addOrder(Long userId, String product, Double amount) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", nextId);
        order.put("userId", userId);
        order.put("product", product);
        order.put("amount", amount);
        order.put("status", "PENDING");
        orders.put(nextId++, order);
    }

    @GetMapping
    public List<Map<String, Object>> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable Long id) {
        Map<String, Object> order = orders.get(id);
        if (order == null) {
            throw new RuntimeException("Order not found: " + id);
        }
        return order;
    }

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getOrdersByUser(@PathVariable Long userId) {
        return orders.values().stream()
                .filter(o -> userId.equals(o.get("userId")))
                .toList();
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", nextId);
        order.put("userId", request.get("userId"));
        order.put("product", request.get("product"));
        order.put("amount", request.get("amount"));
        order.put("status", "PENDING");
        orders.put(nextId++, order);
        return order;
    }
}
