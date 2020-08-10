package io.nats.java.internal;


import java.time.Duration;

public interface Queue <T> {

    QueueMessage<T> next();

    QueueMessage<T> next(Duration duration);

}
