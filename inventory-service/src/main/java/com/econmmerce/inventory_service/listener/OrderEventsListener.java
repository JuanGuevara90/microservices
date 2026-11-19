package com.econmmerce.inventory_service.listener;

import com.econmmerce.inventory_service.event.OrderCancelledEvent;
import com.econmmerce.inventory_service.event.OrderConfirmedEvent;
import com.econmmerce.inventory_service.event.OrderPlacedEvent;
import com.econmmerce.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Slf4j
public class OrderEventsListener {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "inventory-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event){
        log.info("Evento recibido en Inventario para Orden: {} ", event.orderNumber());
        //event.items().forEach(item ->{


            try {

                boolean allProductsInStock = event.items().stream()
                        .allMatch(item ->inventoryService.isInStock(item.sku(),item.quantity()) );


                if(!allProductsInStock){

                    cancelOrder(event,"Stock insuficiente");
                    return;

                }

                event.items().forEach(item ->{
                    inventoryService.reduceStock(item.sku(),item.quantity());
                });


                OrderConfirmedEvent confirmedEvent =  new OrderConfirmedEvent(

                        event.orderNumber(), event.email()
                );

                rabbitTemplate.convertAndSend("order-events","order.confirmed", confirmedEvent);


                log.info("Stock descontado para orden {} - ",event.orderNumber());

            }catch (Exception e){
                log.error("Error inesperado: {}" , e.getMessage());
                cancelOrder(event,"Error tecnico en el procesamiento de inventario");
               // throw  new RuntimeException(e);


            }
       // } );
    }


    private void cancelOrder(OrderPlacedEvent event, String reason){

        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                event.orderNumber(),event.email(), reason
        );

        rabbitTemplate.convertAndSend("order-events","order.cancelled", cancelledEvent);

    }
}
