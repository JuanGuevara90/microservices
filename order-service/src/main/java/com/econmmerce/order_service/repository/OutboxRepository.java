package com.econmmerce.order_service.repository;

import com.econmmerce.order_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    //The events havent sent yet
    List<OutboxEvent>  findByProcessedFalse();
}
