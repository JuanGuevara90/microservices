package com.econmmerce.order_service.service;

import com.econmmerce.order_service.event.OrderPlacedEvent;
import com.econmmerce.order_service.model.OutboxEvent;

import java.util.List;

public interface OutboxService {
    // Save created orders
    void saveOrderPlacedEvent(OrderPlacedEvent event, boolean isProcessed);

    List<OutboxEvent> getPendingEvent();
    void markAsProcessed(Long id);
}
