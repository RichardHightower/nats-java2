package io.nats.java;

public interface Message {

    String getSubject();

    String getReplyTo();

    byte[] getPayload();
}
