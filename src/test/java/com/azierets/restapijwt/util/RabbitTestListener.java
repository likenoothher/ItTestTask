package com.azierets.restapijwt.util;

import lombok.Data;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Data
public class RabbitTestListener {
    private int messageCounter = 0;
    private CountDownLatch countDownLatch;

    @RabbitListener(queues = "${spring.rabbitmq.template.queue-name}")
    public void receiveMessage() {
        countDownLatch.countDown();
        messageCounter++;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void setLatch(CountDownLatch countDownLatch) {
        resetCounter();
        this.countDownLatch = countDownLatch;
    }

    private void resetCounter() {
        messageCounter = 0;
    }
}

