package io.nats.java.internal.actions.client;

import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * Internal action to disconnect the connection.
 */
public class Disconnect implements Action {

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.DISCONNECT;
    }

    public static Disconnect DISCONNECT = new Disconnect();
}
