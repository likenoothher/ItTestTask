package com.azierets.restapijwt.rabbit;

import com.azierets.restapijwt.rabbit.messagedto.RabbitMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMessageSenderImpl implements RabbitMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(RabbitMessage message) {
        rabbitTemplate.convertAndSend(message);
    }
}
