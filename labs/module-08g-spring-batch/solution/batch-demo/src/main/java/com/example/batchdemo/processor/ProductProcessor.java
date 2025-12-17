package com.example.batchdemo.processor;

import com.example.batchdemo.entity.Product;
import com.example.batchdemo.entity.ProductInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductProcessor implements ItemProcessor<ProductInput, Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductProcessor.class);

    @Override
    public Product process(ProductInput input) throws Exception {
        log.debug("Processing product: {}", input.getProductId());

        // Validate required fields
        if (input.getProductId() == null || input.getProductId().isBlank()) {
            log.warn("Skipping record with missing productId");
            return null; // Returning null skips this record
        }

        if (input.getName() == null || input.getName().isBlank()) {
            log.warn("Skipping record {} with missing name", input.getProductId());
            return null;
        }

        // Parse and validate price
        BigDecimal price;
        try {
            if (input.getPrice() == null || input.getPrice().isBlank()) {
                log.warn("Skipping record {} with missing price", input.getProductId());
                return null;
            }
            price = new BigDecimal(input.getPrice());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Skipping record {} with negative price", input.getProductId());
                return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Skipping record {} with invalid price: {}",
                     input.getProductId(), input.getPrice());
            return null;
        }

        // Parse quantity
        Integer quantity = 0;
        try {
            if (input.getQuantity() != null && !input.getQuantity().isBlank()) {
                quantity = Integer.parseInt(input.getQuantity());
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid quantity for {}, defaulting to 0", input.getProductId());
        }

        // Transform: uppercase name, trim description
        Product product = new Product();
        product.setProductId(input.getProductId().trim());
        product.setName(input.getName().trim().toUpperCase());
        product.setDescription(input.getDescription() != null ?
                               input.getDescription().trim() : "");
        product.setPrice(price);
        product.setQuantity(quantity);

        log.info("Processed product: {}", product.getProductId());
        return product;
    }
}
