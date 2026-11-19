package com.econmmerce.order_service.scheduler;


import com.econmmerce.order_service.event.OrderPlacedEvent;
import com.econmmerce.order_service.model.OutboxEvent;
import com.econmmerce.order_service.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageRelayer {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedDelay = 1000)
    public void relayMessage(){
        List<OutboxEvent> pendingEvents = outboxService.getPendingEvent();

        if(!pendingEvents.isEmpty()){

            log.info("Relay > detecto mensaje pendientes {}", pendingEvents.size());


            for(OutboxEvent event : pendingEvents){
                try{
                    OrderPlacedEvent originalEvent = objectMapper.readValue(
                            event.getPayload(),OrderPlacedEvent.class
                    );
                    rabbitTemplate.convertAndSend("order-events", "order.placed",originalEvent);
                    outboxService.markAsProcessed(event.getId());

                    log.info("Mesaje recuperado {}", event.getAggregateId());

                }catch (JacksonException e){
                    log.error("Error  desearializando  {}  {}", event.getAggregateId(), e.getMessage());

                }catch (AmqpException e){
                    log.error("Fallo el reitento {}  {}", event.getAggregateId(), e.getMessage());
                }


            }

        }

    }
}
