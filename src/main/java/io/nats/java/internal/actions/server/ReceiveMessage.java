package io.nats.java.internal.actions.server;


import io.nats.java.Message;
import io.nats.java.internal.Action;
import io.nats.java.internal.ByteUtils;
import io.nats.java.internal.NATSProtocolVerb;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
public class ReceiveMessage implements Action, Message {
    private final String subject; // subject: Subject name this message was received on
    private final String sid; // sid: The unique alphanumeric subscription ID of the subject
    private final String replyTo; // reply-to: The inbox subject on which the publisher is listening for responses
    //#bytes: Size of the payload in bytes
    private final byte[] payload; // payload: The message payload data

    public ReceiveMessage(String subject, String sid, String replyTo, byte[] payload) {
        this.subject = subject;
        this.sid = sid;
        this.replyTo = replyTo;
        this.payload = payload;
    }

    public static ReceiveMessage parse(byte[] bytes) {


        if ((bytes[0] == 'M' ||  bytes[0] == 'm' )
                && (bytes[1] == 'S' ||  bytes[1] == 's')
                && (bytes[2] == 'G' ||  bytes[2] == 'g')) {

            int [] pointer = new int[1];

            final String subject = ByteUtils.readStringDelimByWhitespace(bytes, 3, pointer);
            final String sid = ByteUtils.readStringDelimByWhitespace(bytes, pointer[0], pointer);
            final String msgLengthOrReply = ByteUtils.readStringDelimByWhitespace(bytes, pointer[0], pointer);

            if (Character.isDigit(msgLengthOrReply.charAt(0)) && bytes[pointer[0]] == '\r') {
                int size = Integer.parseInt(msgLengthOrReply);
                final byte[] payload = new byte[size];
                System.arraycopy(bytes, pointer[0] + 2, payload, 0, size);
                return new ReceiveMessage(subject, sid, null, payload);
            } else {
                final String strSize = ByteUtils.readStringDelimByWhitespace(bytes, pointer[0], pointer);
                int size = Integer.parseInt(strSize);
                final byte[] payload = new byte[size];
                System.arraycopy(bytes, pointer[0] + 2, payload, 0, size);
                return new ReceiveMessage(subject, sid, msgLengthOrReply, payload);
            }

        } else {
            throw new IllegalStateException("Unable to parse byte stream for message");
        }



    }

    @Override
    public NATSProtocolVerb verb() {
        return NATSProtocolVerb.MESSAGE;
    }

    public String getSubject() {
        return subject;
    }

    public String getSid() {
        return sid;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "ReceiveMessage{" +
                "subject='" + subject + '\'' +
                ", sid='" + sid + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
