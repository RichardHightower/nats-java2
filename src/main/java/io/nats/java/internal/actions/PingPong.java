package io.nats.java.internal.actions;

import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * PING and PONG implement a simple keep-alive mechanism between client and server. Once a client establishes a
 * connection to the NATS server, the server will continuously send PING messages to the client at a configurable
 * interval. If the client fails to respond with a PONG message within the configured response interval, the server
 * will terminate its connection. If your connection stays idle for too long, it is cut off.
 *
 * If the server sends a ping request, you can reply with a pong message to notify the server that you are still
 * interested. You can also ping the server and will receive a pong reply. The ping/pong interval is configurable.
 * The server uses normal traffic as a ping/pong proxy, so a client that has messages flowing may not receive a
 * ping from the server.
 *
 * Syntax:
 * <pre>
 * PING\r\n
 * PONG\r\n
 * </pre>
 *
 */
public class PingPong {

    public static final Action PING = () -> NATSProtocolVerb.PING;
    public static final Action PONG = () -> NATSProtocolVerb.PONG;

}
