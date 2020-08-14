package io.nats.java.internal.actions.client;


import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * This is sent from the client to the server to unsubscribe to a subject.
 *
 * Unsubscribe. <br />
 *
 * UNSUB. <br />
 *
 * Description: <br />
 * UNSUB unsubscribes the connection from the specified subject, or auto-unsubscribes after the specified number of messages has been received.
 *
 * Syntax:
 * <pre>
 * UNSUB <sid> [max_msgs]
 * </pre>
 *
 * where: <br />
 * sid: The unique alphanumeric subscription ID of the subject to unsubscribe from <br />
 * max_msgs: An optional number of messages to wait for before automatically unsubscribing <br />
 *
 * Example: <br />
 * The following examples concern subject FOO which has been assigned sid 1. To unsubscribe from FOO:
 * <pre>
 * UNSUB 1\r\n
 * </pre>
 * To auto-unsubscribe from FOO after 5 messages have been received:
 * <pre>
 * UNSUB 1 5\r\n
 * </pre>
 */
public class Unsubscribe implements Action {

    private final String sid; // sid : The unique alphanumeric subscription ID of the subject to unsubscribe from
    private final int maxMessages; // max_msgs: An optional number of messages to wait for before automatically unsubscribing

    public Unsubscribe(String sid, int maxMessages) {
        this.sid = sid;
        this.maxMessages = maxMessages;
    }


    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.UNSUBSCRIBE;
    }

    public String getSid() {
        return sid;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    @Override
    public String toString() {
        return "Unsubscribe{" +
                "sid='" + sid + '\'' +
                ", maxMessages=" + maxMessages +
                '}';
    }
}
