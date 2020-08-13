package io.nats.java.internal.actions.server;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class ServerErrorTest {

    @Test
    public void parseError() {
        String message = "-ERR 'Unknown Protocol Operation'\r\n";
        ServerError error = ServerError.parse(message.getBytes(StandardCharsets.UTF_8));

        assertEquals("Unknown Protocol Operation", error.getMessage());
    }
}