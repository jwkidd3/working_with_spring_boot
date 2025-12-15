package com.example.feignclient.controller;

import com.example.feignclient.client.ProductClient;
import com.example.feignclient.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ProductClient productClient;

    public StoreController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productClient.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productClient.getProduct(id);
    }

    @GetMapping("/products/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productClient.getProductsByCategory(category);
    }

    @GetMapping("/catalog")
    public Map<String, Object> getCatalog() {
        Map<String, Object> catalog = new HashMap<>();
        List<Product> products = productClient.getAllProducts();
        catalog.put("totalProducts", products.size());
        catalog.put("products", products);
        return catalog;
    }
}
