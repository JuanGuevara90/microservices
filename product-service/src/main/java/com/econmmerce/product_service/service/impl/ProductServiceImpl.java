package com.econmmerce.product_service.service.impl;

import com.econmmerce.product_service.dto.ProductRequestDTO;
import com.econmmerce.product_service.dto.ProductResponseDTO;
import com.econmmerce.product_service.exception.ResourceNotFoundException;
import com.econmmerce.product_service.mapper.ProductMapper;
import com.econmmerce.product_service.model.Product;
import com.econmmerce.product_service.repository.ProductRepository;
import com.econmmerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final ProductMapper mapper;
    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {

        Product product = mapper.toProduct(requestDTO);
        Product saveProduct = repository.save(product);

        log.info("Producto {} guardado",saveProduct.getName());
        return mapper.toProductResponseDTO(saveProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllsProducts() {
        return repository.findAll().
                stream()
                .map(mapper::toProductResponseDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO getProductsById(String id) {
        Product product = repository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Producto","id",id)
        );

        return mapper.toProductResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO productRequest) {

        Product product = repository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Producto","id",id)
        );

        mapper.updateProductFromRequest(productRequest,product);

        Product updateProduct = repository.save(product);

        log.info("Producto {} actualizado",updateProduct.getName());


        return mapper.toProductResponseDTO(updateProduct);
    }

    @Override
    public void deleteProduct(String id) {

        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Producto","id",id);
        }

        repository.deleteById(id);

        log.info("Producto con el id: {} ",id);

    }
}
