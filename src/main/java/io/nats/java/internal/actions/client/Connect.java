package io.nats.java.internal.actions.client;

import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/** This is sent from the client to the server after the client gets the first INFO from the server which is after it connects.
 *
 *  1. <<establish TCP connection>>
 *  2. Server Sends ServerInformation to Client.
 *  3. Client Sends Connect to Server.
 *
 * The CONNECT message is the client version of the INFO message. Once the client has established a TCP/IP socket
 * connection with the NATS server, and an INFO message has been received from the server, the client may send a
 * CONNECT message to the NATS server to provide more information about the current connection as well as security
 * information.
 */
public class Connect implements Action {

    private final boolean verbose; // verbose: Turns on +OK protocol acknowledgements.
    private final boolean pedantic; //      pedantic: Turns on additional strict format checking, e.g. for properly formed subjects
    private final boolean tlsRequired; // tls_required: Indicates whether the client requires an SSL connection.
    private final String authToken; // auth_token: Client authorization token (if auth_required is set)
    private final String user; // user : Connection username (if auth_required is set)
    private final char [] password; //pass : Connection password (if auth_required is set)
    private final String name; // name : Optional client name
    private final String lang; // lang : The implementation language of the client.
    private final String version; // version: The version of the client.
    private final int protocol; //        protocol: optional int. Sending 0 (or absent) indicates client supports original protocol. Sending 1 indicates that the client supports dynamic reconfiguration of cluster topology changes by asynchronously receiving INFO messages with known servers it can reconnect to.
    private final boolean echo; //        echo: Optional boolean. If set to true, the server (version 1.2.0+) will not send originating messages from this connection to its own subscriptions. Clients should set this to true only for server supporting this feature, which is when proto in the INFO protocol is set to at least 1.
    private final String sig; // sig: In case the server has responded with a nonce on INFO, then a NATS client must use this field to reply with the signed nonce.
    private final String jwt; //        jwt: The JWT that identifies a user permissions and acccount.

    public Connect(boolean verbose, boolean pedantic, boolean tlsRequired, String authToken, String user, char[] password, String name, String lang, String version, int protocol, boolean echo, String sig, String jwt) {
        this.verbose = verbose;
        this.pedantic = pedantic;
        this.tlsRequired = tlsRequired;
        this.authToken = authToken;
        this.user = user;
        this.password = password;
        this.name = name;
        this.lang = lang;
        this.version = version;
        this.protocol = protocol;
        this.echo = echo;
        this.sig = sig;
        this.jwt = jwt;
    }

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.CONNECT;
    }

}
