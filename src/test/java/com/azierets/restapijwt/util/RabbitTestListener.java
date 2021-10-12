package com.azierets.restapijwt.util;

import lombok.Data;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Data
public class RabbitTestListener {
    private int messageCounter = 0;

    @RabbitListener(queues = "${spring.rabbitmq.template.queue-name}")
    public void receiveMessage() {
        messageCounter++;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void resetCounter() {
        messageCounter = 0;
    }
}

