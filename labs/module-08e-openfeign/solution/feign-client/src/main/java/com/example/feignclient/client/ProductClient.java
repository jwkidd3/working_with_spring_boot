package com.example.feignclient.client;

import com.example.feignclient.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products")
    List<Product> getAllProducts();

    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products/category/{category}")
    List<Product> getProductsByCategory(@PathVariable("category") String category);

    @PostMapping("/api/products")
    Product createProduct(@RequestBody Product product);

    @PutMapping("/api/products/{id}")
    Product updateProduct(@PathVariable("id") Long id, @RequestBody Product product);

    @DeleteMapping("/api/products/{id}")
    void deleteProduct(@PathVariable("id") Long id);
}
