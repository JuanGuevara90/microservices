package com.econmmerce.product_service.dataloader;

import com.econmmerce.product_service.model.Product;
import com.econmmerce.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {

        Product product = Product.builder()
                .name("Honor Magic 7 lite")
                .description("IA")
                .price(BigDecimal.valueOf(300))
                .build();

        productRepository.save(product);

        System.out.println("Datos prueba "+ product.getName());


    }
}
