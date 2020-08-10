package io.nats.java.internal;


import io.nats.java.internal.NATSProtocolVerb;

public interface Action {
    NATSProtocolVerb verb();

}
