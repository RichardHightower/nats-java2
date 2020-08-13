package io.nats.java.internal.actions.server;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;


public class ReceiveMessageTest {


    @Test
    public void parseWithReply() {
        String message = "MSG 1.FOO.BAR 9 INBOX.34 11\r\nHello World\r\n";
        ReceiveMessage receiveMessage = ReceiveMessage.parse(message.getBytes(StandardCharsets.UTF_8));

        System.out.println(receiveMessage);

        assertEquals("1.FOO.BAR", receiveMessage.getSubject());
        assertEquals("9", receiveMessage.getSid());
        assertEquals("INBOX.34", receiveMessage.getReplyTo());
        assertEquals("Hello World", new String(receiveMessage.getPayload(), StandardCharsets.UTF_8));
        assertEquals(11, new String(receiveMessage.getPayload(), StandardCharsets.UTF_8).length());

    }

    @Test
    public void parseNoReply() {
        String message = "MsG \t 1.FOO.BAR 9    11\r\nHello World\r\n";
        ReceiveMessage receiveMessage = ReceiveMessage.parse(message.getBytes(StandardCharsets.UTF_8));

        assertEquals("1.FOO.BAR", receiveMessage.getSubject());
        assertEquals("9", receiveMessage.getSid());
        assertNull(receiveMessage.getReplyTo());
        assertEquals("Hello World", new String(receiveMessage.getPayload(), StandardCharsets.UTF_8));
        assertEquals(11, new String(receiveMessage.getPayload(), StandardCharsets.UTF_8).length());

    }
}