package com.azierets.restapijwt.rabbit.messagedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggingMessageDto implements Serializable, RabbitMessage {
    private String user;
    private long loggingTime;
}
