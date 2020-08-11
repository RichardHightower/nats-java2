package io.nats.java.internal.actions.server;


import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * Message sent from server to client. <br />
 *
 * Error <br />
 * +ERR <br />
 *
 * Description: <br />

 * The -ERR message is used by the server indicate a protocol, authorization, or other runtime connection error to
 * the client. Most of these errors result in the server closing the connection.
 * Handling of these errors usually has to be done asynchronously.
 *
 * Syntax
 * <pr>
 * -ERR <error message>
 * </pr>
 *
 *
 * Some protocol errors result in the server closing the connection. Upon receiving these errors, the connection is no
 * longer valid and the client should clean up relevant resources. These errors include: <br />
 * -ERR 'Unknown Protocol Operation': Unknown protocol error <br />
 * -ERR 'Attempted To Connect To Route Port': Client attempted to connect to a route port instead of the client port <br />
 * -ERR 'Authorization Violation': Client failed to authenticate to the server with credentials specified in the CONNECT message <br />
 * -ERR 'Authorization Timeout': Client took too long to authenticate to the server after establishing a connection (default 1 second) <br />
 * -ERR 'Invalid Client Protocol': Client specified an invalid protocol version in the CONNECT message <br />
 * -ERR 'Maximum Control Line Exceeded': Message destination subject and reply subject length exceeded the maximum
 * control line value specified by the max_control_line server option.  The default is 1024 bytes. <br />
 * -ERR 'Parser Error': Cannot parse the protocol message sent by the client <br />
 * -ERR 'Secure Connection - TLS Required':  The server requires TLS and the client does not have TLS enabled. <br />
 * -ERR 'Stale Connection': The server hasn't received a message from the client, including a PONG in too long. <br />
 * -ERR 'Maximum Connections Exceeded': This error is sent by the server when creating a new connection and the server
 * has exceeded the maximum number of connections specified by the max_connections server option.  The default is 64k. <br />
 * -ERR 'Slow Consumer': The server pending data size for the connection has reached the maximum size (default 10MB). <br />
 * -ERR 'Maximum Payload Violation': Client attempted to publish a message with a payload size that exceeds the max_payload
 * size configured on the server. This value is supplied to the client upon connection in the initial INFO message.
 * The client is expected to do proper accounting of byte size to be sent to the server in order to handle this error synchronously. <br />
 * Protocol error messages where the connection remains open are listed below. The client should not close the connection in these cases.
 * -ERR 'Invalid Subject': Client sent a malformed subject (e.g. sub foo. 90) <br />
 * -ERR 'Permissions Violation for Subscription to <subject>': The user specified in the CONNECT message does not have
 * permission to subscribe to the subject. <br />
 * -ERR 'Permissions Violation for Publish to <subject>': The user specified in the CONNECT message does not have
 * permissions to publish to the subject. <br />
 */
public class ServerError implements Action {

    private final String message;
    private final ErrorType errorType;

    public ServerError(String message, ErrorType errorType) {
        this.message = message;
        this.errorType = errorType;
    }

    public static ServerError parse(byte[] bytes) {
        return null;
    }

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.ERROR;
    }

    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
