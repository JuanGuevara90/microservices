package com.econmmerce.product_service.mapper;

import com.econmmerce.product_service.dto.ProductRequestDTO;
import com.econmmerce.product_service.dto.ProductResponseDTO;
import org.mapstruct.Mapper;
import com.econmmerce.product_service.model.Product;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toProduct(ProductRequestDTO requestDTO);

    ProductResponseDTO toProductResponseDTO(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequestDTO productRequestDTO,@MappingTarget Product product);
}
