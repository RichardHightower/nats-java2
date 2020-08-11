package io.nats.java;


import java.time.Duration;

public interface InputQueue<T> {

    InputQueueMessage<T> next();

    InputQueueMessage<T> next(Duration duration);

}
