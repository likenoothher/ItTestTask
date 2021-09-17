package com.azierets.restapijwt.rabbit;

import com.azierets.restapijwt.rabbit.messagedto.RabbitMessage;

public interface RabbitMessageSender {
    void sendMessage(RabbitMessage message);
}
