package io.nats.java.internal;



public interface Action extends VerbHolder {
    NATSProtocolVerb verb();
}
