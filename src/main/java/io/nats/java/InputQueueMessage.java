package io.nats.java;

public interface InputQueueMessage<T> {
    default boolean isError() {return false;}
    default boolean isPresent() {return false;}
    default boolean isDone() {return false;}
    default Exception error() {return null;}
    default T value() {return null;}
}
