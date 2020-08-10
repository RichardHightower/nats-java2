package io.nats.java.internal.actions.server;

import io.nats.java.internal.Action;
import io.nats.java.ConnectURL;
import io.nats.java.internal.NATSProtocolVerb;

import java.util.List;

/** This is sent from the server to the client when the client first connects.
 *
 *  1. <<establish TCP connection>>
 *  2. Server Sends ServerInformation to Client.
 *
 *  OR
 *
 *  1. <<anytime for example cluster topology changes>>
 *  2. Server Sends ServerInformation to Client.
 *
 *  A client will need to start as a plain TCP connection, then when the server accepts a connection from the client,
 *  it will send information about itself, the configuration and security requirements necessary for the client to
 *  successfully authenticate with the server and exchange messages.
 *
 *  When using the updated client protocol (see CONNECT below), INFO messages can be sent anytime by the server.
 *  This means clients with that protocol level need to be able to asynchronously handle INFO messages.
 */
public class ServerInformation implements Action {

    private final String serverId; //server_id: The unique identifier of the NATS server
    private final String version; // The version of the NATS server
    private final String go; //The version of golang the NATS server was built with
    private final String host; // The IP address used to start the NATS server, by default this will be 0.0.0.0 and can be configured with -client_advertise host:port
    private final int port; // The port number the NATS server is configured to listen on
    private final int maxPayloadBytes; //max_payload // Maximum payload size, in bytes, that the server will accept from the client.
    private final int proto; // proto: An integer indicating the protocol version of the server. The server version 1.2.0 sets this to 1 to indicate that it supports the "Echo" feature.
    private final long clientId;   //     client_id: An optional unsigned integer (64 bits) representing the internal client identifier in the server. This can be used to filter client connections in monitoring, correlate with error logs, etc...
    private final boolean authRequired; //auth_required: If this is set, then the client should try to authenticate upon connect.
    private final boolean tlsRequired; //tls_required: If this is set, then the client must perform the TLS/1.2 handshake. Note, this used to be ssl_required and has been updated along with the protocol from SSL to TLS.
    private final boolean tlsVerify; //tls_verify; // If this is set, the client must provide a valid certificate during the TLS handshake.
    private final List<ConnectURL> connectUrls; // connect_urls : An optional list of server urls that a client can connect to.
    private final boolean lameDuckMode; // ldm: If the server supports Lame Duck Mode notifications, and the current server has transitioned to lame duck, ldm will be set to true.

    public ServerInformation(String serverId, String version, String go, String host, int port, int maxPayloadBytes, int proto,
                             long clientId, boolean authRequired, boolean tlsRequired, boolean tlsVerify, List<ConnectURL> connectUrls,
                             boolean lameDuckMode) {
        this.serverId = serverId;
        this.version = version;
        this.go = go;
        this.host = host;
        this.port = port;
        this.maxPayloadBytes = maxPayloadBytes;
        this.proto = proto;
        this.clientId = clientId;
        this.authRequired = authRequired;
        this.tlsRequired = tlsRequired;
        this.tlsVerify = tlsVerify;
        this.connectUrls = connectUrls;
        this.lameDuckMode = lameDuckMode;
    }

    public static ServerInformation parse(byte[] bytes) {
        //TODO
        return null;
    }

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.INFO;
    }

    public ServerInformation merge(final ServerInformation information) {
        final String serverId = information.serverId == null ? this.serverId : information.serverId;
        final String version = information.version == null ? this.version : information.version;
        final String go = information.go == null ? this.go : information.go;
        final String host = information.host == null ? this.host : information.host;
        final int port = information.port == -1 ? this.port : information.port;
        final int maxPayloadBytes = information.maxPayloadBytes == -1 ? this.maxPayloadBytes : information.maxPayloadBytes;
        final long clientId = information.clientId == -1 ? this.clientId : information.clientId;
        final int proto = information.proto == -1 ? this.proto : information.proto;
        final boolean authRequired = information.authRequired || this.authRequired;
        final boolean tlsRequired = information.tlsRequired || this.tlsRequired;
        final boolean tlsVerify = information.tlsVerify || this.tlsVerify;
        final List<ConnectURL> connectUrls = information.connectUrls == null ? this.connectUrls : information.connectUrls;
        final boolean lameDuckMode = information.lameDuckMode;
        return new ServerInformation(serverId,version, go, host, port, maxPayloadBytes,
                proto, clientId, authRequired, tlsRequired, tlsVerify, connectUrls, lameDuckMode);
    }
}
