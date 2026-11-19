package com.econmmerce.order_service.event;

public record OrderCancelledEvent(String orderNumber, String email , String reason ) {
}
