package io.nats.java.internal;


import java.time.Duration;

public interface InputQueue<T> {

    InputQueueMessage<T> next();

    InputQueueMessage<T> next(Duration duration);


}
