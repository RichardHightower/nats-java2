package io.nats.java;

public interface ClientErrorHandler {
    void handleError(Exception exception);
}
