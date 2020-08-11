package io.nats.java.internal.actions.client;


import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * Publish a message from the client to the server.
 *
 * The PUB message publishes the message payload to the given subject name, optionally supplying a reply subject.
 * If a reply subject is supplied, it will be delivered to eligible subscribers along with the supplied payload.
 * Note that the payload itself is optional. To omit the payload, set the payload size to 0, but the second CRLF
 * is still required.
 *
 */
public class Publish implements Action {

    private final String subject; // subject : The destination subject to publish to
    private final String replyTo;  //reply-to: The optional reply inbox subject that subscribers can use to send a response back to the publisher/requestor
    //#bytes: The payload size in bytes stored in length of payload.
    private final byte[] payload; // The message payload data



    public Publish(String subject, String replyTo, byte[] payload) {
        this.subject = subject;
        this.replyTo = replyTo;
        this.payload = payload;
    }

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.PUBLISH;
    }

    public String getSubject() {
        return subject;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public byte[] getPayload() {
        return payload;
    }

}
