package io.nats.java.internal;

public interface InputQueueMessage<T> {
    boolean isError();
    boolean isPresent();
    boolean doStop();
    Exception error();
    T value();
}
