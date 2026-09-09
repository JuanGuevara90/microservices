package com.econmmerce.product_service.service;

import com.econmmerce.product_service.dto.ProductRequestDTO;
import com.econmmerce.product_service.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);
    List<ProductResponseDTO> getAllsProducts();
    ProductResponseDTO getProductsById(String id);
    ProductResponseDTO updateProduct(String id,ProductRequestDTO requestDTO);
    void deleteProduct(String id);



}
