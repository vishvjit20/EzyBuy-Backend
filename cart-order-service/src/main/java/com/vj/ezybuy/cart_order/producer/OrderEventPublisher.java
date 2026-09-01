package com.vj.ezybuy.cart_order.producer;


import com.vj.ezybuy.common.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final Logger logger = Logger.getLogger(OrderEventPublisher.class.getName());

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ORDER_TOPIC = "order_created";

    public void publishOrderCreatedEvent(OrderEvent orderEvent) {
        try {
            kafkaTemplate.send(ORDER_TOPIC, orderEvent);
            logger.info("Order Event Published Successfully: " + orderEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
