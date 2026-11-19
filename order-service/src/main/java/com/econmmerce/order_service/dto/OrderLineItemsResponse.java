package com.econmmerce.order_service.dto;


import com.econmmerce.order_service.model.OrderStatus;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsResponse {
    private Long id; // AQUÍ SÍ VA EL ID
    private String sku;
    private BigDecimal price;
    private OrderStatus status;
    private Integer quantity;
}