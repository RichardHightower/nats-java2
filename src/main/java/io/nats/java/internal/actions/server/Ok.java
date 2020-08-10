package io.nats.java.internal.actions.server;


import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 *  OK
 *  <br />
 *
 *  +OK
 *  <br />
 *
 *  When the verbose connection option is set to true (the default value), the server acknowledges each well-formed
 *  protocol message from the client with a +OK message. Most NATS clients set the verbose option to false using
 *  the CONNECT message
 */
public class Ok implements Action {
    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.OK;
    }
}
