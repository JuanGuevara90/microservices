package com.ecommerce.notification_service.listener;


import com.ecommerce.notification_service.event.OrderCancelledEvent;
import com.ecommerce.notification_service.event.OrderConfirmedEvent;
import com.ecommerce.notification_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = "notification-queue")
public class OrderEventsListener {


    private final JavaMailSender mailSender;


    //@RabbitListener(queues = "notification-queue")

    @RabbitHandler
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event){
        log.info("Pedido confirmado {} ", event.orderNumber());


       // throw  new RuntimeException("Error SMTP");


        //event.items().forEach(item ->{


        // try {

     SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("pedidos@ecommerce.com");
        message.setTo(event.email());
        message.setSubject("Order confirmado -" + event.orderNumber());
        message.setText("Hola que tal ");
        mailSender.send(message);


        log.info("Enviando correo de cancelacion a:  {} ", event.email());

        log.info("Correo enviado por la orden :  {} ", event.orderNumber());


          /*  }catch (Exception e){
                log.error("Error al enviar correo{}" , e.getMessage());

               // throw  new RuntimeException(e);


            }*/
        // } );
    }

    @RabbitHandler
    public void handleOrderCancelledEvent(OrderCancelledEvent event){
        log.info("Pedido cancelado {} ", event.orderNumber());

        //try {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("pedidos@ecommerce.com");
        message.setTo(event.email());
        message.setSubject("Order cancelada -" + event.orderNumber());
        message.setText("Hola que tal ");
        mailSender.send(message);


        log.info("Enviando correo de confirmacion a:  {} ", event.email());

        log.info("Correo enviado por la orden :  {} ", event.orderNumber());


       /* }catch (Exception e){
            log.error("Error al enviar correo{}" , e.getMessage());

            // throw  new RuntimeException(e);


        }*/
        // } );
    }
}
