package com.econmmerce.product_service.controller;

import com.econmmerce.product_service.dto.ProductRequestDTO;
import com.econmmerce.product_service.dto.ProductResponseDTO;
import com.econmmerce.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@RefreshScope
public class ProductController {

    private final ProductService productService;

    @Value("${app.maintenance.message: Sistema Operativo}")
    private String maintenanceMessage; //maintenance

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@RequestBody @Valid ProductRequestDTO productRequestDTO){
        return productService.createProduct(productRequestDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDTO>  getAllProducts(HttpServletResponse response){
        response.addHeader("X-Maintenance-Message", maintenanceMessage);

        return  productService.getAllsProducts();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO  getProductById(@PathVariable String id){
        return  productService.getProductsById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void  deleteProduct(@PathVariable String id){
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO  getProductById(@RequestBody @Valid  ProductRequestDTO productRequestDTO,@PathVariable String id){
        return  productService.updateProduct(id,productRequestDTO);
    }


    @GetMapping("/test-fail")
    public void  testFail(){
        throw new RuntimeException("Boom la base de datos exploto (Similacro)");
    }




}
