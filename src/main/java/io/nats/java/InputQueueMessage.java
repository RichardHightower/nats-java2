package io.nats.java;

public interface InputQueueMessage<T> {
    boolean isError();
    boolean isPresent();
    boolean isDone();
    Exception error();
    T value();
}
