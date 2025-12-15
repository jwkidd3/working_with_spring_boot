package com.example.feignclient.client;

import com.example.feignclient.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public List<Product> getAllProducts() {
        logger.warn("Fallback: getAllProducts");
        return Collections.emptyList();
    }

    @Override
    public Product getProduct(Long id) {
        logger.warn("Fallback: getProduct for id {}", id);
        Product fallback = new Product();
        fallback.setId(id);
        fallback.setName("Unknown Product");
        fallback.setDescription("Product service unavailable");
        fallback.setPrice(BigDecimal.ZERO);
        fallback.setCategory("Unknown");
        fallback.setStockQuantity(0);
        return fallback;
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        logger.warn("Fallback: getProductsByCategory for {}", category);
        return Collections.emptyList();
    }

    @Override
    public Product createProduct(Product product) {
        logger.warn("Fallback: createProduct");
        return null;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        logger.warn("Fallback: updateProduct for id {}", id);
        return null;
    }

    @Override
    public void deleteProduct(Long id) {
        logger.warn("Fallback: deleteProduct for id {}", id);
    }
}
