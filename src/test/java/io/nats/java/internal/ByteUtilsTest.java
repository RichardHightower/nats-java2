package io.nats.java.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ByteUtilsTest {

//    @Before
//    public void setUp() throws Exception {
//    }
//
//    @After
//    public void tearDown() throws Exception {
//    }

    @Test
    public void parseMessageSize() throws IOException {
//                      01234567890123 4 56 7 8
        byte[] bytes = "MSG su si re 1\r\nA\r\n".getBytes(StandardCharsets.UTF_8);

        int [] location = new int[1];
        int [] indexes = new int[10];
        int[] numFeatures = new int[1];



        int size = ByteUtils.readSizeFromMsgAndValidate(bytes, location, bytes.length, indexes, numFeatures);

        assertEquals(1, size);
        assertEquals(5, numFeatures[0]);

        assertArrayEquals(new int[]{4,6,7,9,10,12,13,14,16,17}, indexes);

        final ServerMessage serverMessage = new ServerMessage(bytes, NATSProtocolVerb.MESSAGE, indexes);

        assertEquals("su", serverMessage.firstArgAsString());
        assertEquals("si", serverMessage.secondArgAsString());
        assertEquals("re", serverMessage.thirdArgAsString());
        assertEquals("1", serverMessage.fourthArgAsString());
        assertArrayEquals(new byte[]{'1'}, serverMessage.fourthArgAsBytes());
        assertEquals("A", serverMessage.fifthArgAsString());
        assertArrayEquals(new byte[]{'A'}, serverMessage.fifthArgAsBytes());


    }
}