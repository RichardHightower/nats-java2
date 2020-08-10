package io.nats.java.internal.actions;

public interface OutputQueue <T> {
    void send(T item);
    boolean isClosed();
}
