package com.azierets.restapijwt.rabbit.messagedto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class LoggingMessageDto implements Serializable, RabbitMessage {
    private String user;
    private long loggingTime;
}
