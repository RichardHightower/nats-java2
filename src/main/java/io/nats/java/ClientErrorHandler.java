package io.nats.java;


public interface ClientErrorHandler {
     void handleException(Exception exception);
}