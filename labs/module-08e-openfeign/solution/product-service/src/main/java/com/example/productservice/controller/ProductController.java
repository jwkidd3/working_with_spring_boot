package com.example.productservice.controller;

import com.example.productservice.model.Product;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final Map<Long, Product> products = new HashMap<>();

    public ProductController() {
        products.put(1L, new Product(1L, "Laptop", "High-performance laptop",
                new BigDecimal("999.99"), "Electronics", 50));
        products.put(2L, new Product(2L, "Smartphone", "Latest smartphone",
                new BigDecimal("699.99"), "Electronics", 100));
        products.put(3L, new Product(3L, "Headphones", "Wireless headphones",
                new BigDecimal("199.99"), "Electronics", 200));
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new RuntimeException("Product not found: " + id);
        }
        return product;
    }

    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return products.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        long newId = products.keySet().stream().max(Long::compare).orElse(0L) + 1;
        product.setId(newId);
        products.put(newId, product);
        return product;
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        if (!products.containsKey(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        product.setId(id);
        products.put(id, product);
        return product;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        products.remove(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        response.put("productId", id.toString());
        return response;
    }
}
