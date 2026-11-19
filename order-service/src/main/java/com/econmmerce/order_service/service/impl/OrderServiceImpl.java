package com.econmmerce.order_service.service.impl;

import com.econmmerce.order_service.dto.OrderLineItemsResponse;
import com.econmmerce.order_service.dto.OrderRequest;
import com.econmmerce.order_service.dto.OrderResponse;
import com.econmmerce.order_service.event.OrderPlacedEvent;
import com.econmmerce.order_service.exception.ResourceNotFoundException;
import com.econmmerce.order_service.mapper.OrderMapper;
import com.econmmerce.order_service.model.Order;
import com.econmmerce.order_service.model.OrderLineItems;
import com.econmmerce.order_service.model.OrderStatus;
import com.econmmerce.order_service.repository.OrderRepository;
import com.econmmerce.order_service.service.OrderService;
import com.econmmerce.order_service.service.OutboxService;
import com.econmmerce.order_service.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    //private final InventoryClient inventoryClient;
    private final RabbitTemplate rabbitTemplate;

    private final OutboxService outboxService;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    /*public CompletableFuture<OrderResponse> fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {
        return CompletableFuture.supplyAsync(()->{
            log.error("Circuit Breaket activado. Causa :{}", throwable.getMessage());

            throw  new RuntimeException("El Servicio de Inventario no responde. Por favor intente mas tarde.");
        });

    }*/

    public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {

            log.error("Circuit Breaket activado. Causa :{}", throwable.getMessage());

            throw  new RuntimeException("El Servicio de Inventario no responde. Por favor intente mas tarde.");


    }


    /*
    @Override
    @Transactional
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    @Retry(name="inventory")
    @TimeLimiter(name="inventory")
    public CompletableFuture<OrderResponse> placeOrder(OrderRequest orderRequest, String userId) {

        long startTime = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(()->{

                    if(!ordersEnabled){
                        log.warn("Pedido rechazado: Servicio deshabilitado por configuracion");
                        throw new RuntimeException("El servicio de pedidos esta actualment en mantenimiento. Intente mas tarde");
                    }

                    log.info("Colocando nuevo pedido");

                    Order order = orderMapper.toOrder(orderRequest);

                    order.setUserId(userId);

                    for(var item : order.getOrderLineItemsList()){
                        String sku = item.getSku();
                        Integer quantity = item.getQuantity();


                        try{
                            //webClientBuilder.build().put()
                            //       .uri("http://localhost:8082/api/v1/inventory/reduce/"+ sku,
                            //              uriBuilder -> uriBuilder.queryParam("quantity", quantity).build())
                            //     .retrieve()
                            //    .toBodilessEntity()
                            //   .block();

                            inventoryClient.reduceStock(sku,quantity);

                        }catch (Exception e){

                            log.error("Error al reducir stock para el producto {}: {}", sku, e.getMessage());

                            throw new IllegalArgumentException("No se pudo procesar la orden: Stock insuficiente o error de inventario ");


                        }


                    }

                    order.setOrderNumber(UUID.randomUUID().toString());

                    long totatlTime = System.currentTimeMillis() - startTime;

                    if(totatlTime> 3000){
                        log.warn("Timeout detectado internamente {} ms . Abortado guardado en BD",totatlTime );
                        throw new RuntimeException("Timeout excedido - Rollback manual");
                    }

                    Order savedOrder = orderRepository.save(order);

                    log.info("Orden guardada con éxito. ID: {}", savedOrder.getId());

                    return orderMapper.toOrderResponse(savedOrder);


                }
                );
    }


    */



    // Spring MVC / Tomcat//// Arquitectura Sincrona   /// TimeLimit cancela espera del cliente pero no mata el hilo del seridor
    @Override
    @Transactional
    /*@CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    @Retry(name="inventory")*/
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {

                    if(!ordersEnabled){
                        log.warn("Pedido rechazado: Servicio deshabilitado por configuracion");
                        throw new RuntimeException("El servicio de pedidos esta actualment en mantenimiento. Intente mas tarde");
                    }

                    log.info("Colocando nuevo pedido");

                    Order order = orderMapper.toOrder(orderRequest);

                    order.setUserId(userId);

                    /*for(var item : order.getOrderLineItemsList()){
                        String sku = item.getSku();
                        Integer quantity = item.getQuantity();


                        try{
                            //webClientBuilder.build().put()
                            //       .uri("http://localhost:8082/api/v1/inventory/reduce/"+ sku,
                            //              uriBuilder -> uriBuilder.queryParam("quantity", quantity).build())
                            //     .retrieve()
                            //    .toBodilessEntity()
                            //   .block();

                            inventoryClient.reduceStock(sku,quantity);

                        }catch (Exception e){

                            log.error("Error al reducir stock para el producto {}: {}", sku, e.getMessage());

                            throw new IllegalArgumentException("No se pudo procesar la orden: Stock insuficiente o error de inventario ");


                        }


                    }*/

                    order.setOrderNumber(UUID.randomUUID().toString());

                    order.setStatus(OrderStatus.PLACED);


                    Order savedOrder = orderRepository.save(order);

                    log.info("Orden guardada con éxito. ID: {} Status; {}", savedOrder.getId(), savedOrder.getStatus().toString());

                    //Contratos
                    List<OrderPlacedEvent.OrderItemEvent> orderItems =
                            order.getOrderLineItemsList().stream()
                                    .map(item-> new OrderPlacedEvent.OrderItemEvent(
                                            item.getSku(), item.getPrice().toString(),item.getQuantity()

                                    )).toList();

                    OrderPlacedEvent event = new OrderPlacedEvent(
                            savedOrder.getOrderNumber(), orderRequest.getEmail(), orderItems
                    );



                    boolean sendToRabbit = false;
                    try{
                        rabbitTemplate.convertAndSend(
                                "order-events",
                                "order.placed",
                                event
                        );

                        sendToRabbit = true;

                        log.info("Mensaje enviado a rabbitmq: {}", savedOrder.getOrderNumber());



                    }catch (AmqpException e){
                        log.error("Rabbitmq esta caido , orderNum {} ",savedOrder.getOrderNumber());

                    }
                    outboxService.saveOrderPlacedEvent(event,sendToRabbit);


                    log.info("Evento enviado  RabbitMqa para la orden {}", savedOrder.getOrderNumber());

                    return orderMapper.toOrderResponse(savedOrder);

    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String userId, boolean isAdmin) {

        List<Order> orders;

        if(isAdmin){
            orders = orderRepository.findAll();
        }else{
            orders = orderRepository.findByUserId(userId);
        }
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

   /* @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }*/

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden", "id", id);
        }
        orderRepository.deleteById(id);
        log.info("Orden eliminada. ID: {}", id);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderNumber, OrderStatus newStatus) {

        log.info("Actualizando base de datos: Order {} -> {}", orderNumber, newStatus);

        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(
                order -> {
                    order.setStatus(newStatus);
                    orderRepository.save(order);
                    log.info("Estado actualizado en DB para la ordern {}", orderNumber);
                },
                ()-> log.error("No se encontro la orden {} ", orderNumber)
        );

    }
}