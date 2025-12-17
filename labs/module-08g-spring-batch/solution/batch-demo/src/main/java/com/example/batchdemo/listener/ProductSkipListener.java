package com.example.batchdemo.listener;

import com.example.batchdemo.entity.Product;
import com.example.batchdemo.entity.ProductInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class ProductSkipListener implements SkipListener<ProductInput, Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Skipped during READ: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(ProductInput item, Throwable t) {
        log.error("Skipped during PROCESS - Item: {}, Error: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Product item, Throwable t) {
        log.error("Skipped during WRITE - Item: {}, Error: {}", item, t.getMessage());
    }
}
