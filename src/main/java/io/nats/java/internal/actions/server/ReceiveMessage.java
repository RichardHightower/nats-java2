package io.nats.java.internal.actions.server;


import io.nats.java.internal.Action;
import io.nats.java.internal.NATSProtocolVerb;

/**
 * ReceiveMessage <br />
 * MSG <br />
 * <br />
 * Description: <br />
 * The MSG protocol message is used to deliver an application message to the client.
 * <br />
 * Syntax: <br />
 * <pre>
 * MSG <subject> <sid> [reply-to] <#bytes>\r\n[payload]\r\n
 *
 * </pre>
 * <br />
 *
 * where:<br />
 * subject: Subject name this message was received on <br />
 * sid: The unique alphanumeric subscription ID of the subject <br />
 * reply-to: The inbox subject on which the publisher is listening for responses <br />
 * #bytes: Size of the payload in bytes <br />
 * payload: The message payload data <br />
 * <br />
 * <br />
 * Example: <br />
 * The following message delivers an application message from subject FOO.BAR:
 * <pr>
 * MSG FOO.BAR 9 11\r\nHello World\r\n
 * </pr>
 * To deliver the same message along with a reply inbox:
 * <pr>
 * MSG FOO.BAR 9 INBOX.34 11\r\nHello World\r\n
 * </pr>
 */
public class ReceiveMessage implements Action {

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.MESSAGE;
    }

}
