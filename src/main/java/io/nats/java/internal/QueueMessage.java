package io.nats.java.internal;

public interface QueueMessage <T> {
    boolean isError();
    boolean isPresent();
    boolean doStop();
    Exception error();
    T value();
}
