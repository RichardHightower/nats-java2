package io.nats.java.internal.actions;

import io.nats.java.internal.ByteUtils;
import io.nats.java.internal.io.InputStreamUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class InputStreamUtilsTest {

    @Test
    public void readLine() throws IOException {
                                                                         //01234 5 6
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Hello\r\n".getBytes(StandardCharsets.UTF_8));

        byte [] buffer = new byte[512];
        int [] totalBytesRead = new int[1];
        totalBytesRead[0] = -1;

        int position = InputStreamUtils.readLine(inputStream, buffer, 0, totalBytesRead);

        assertEquals(6, position);

        final String line = new String(buffer, 0, position+1, StandardCharsets.UTF_8);

        assertEquals("Hello\r\n", line);


    }

    @Test
    public void read2Lines() throws IOException {
                                                                         //01234 5 678901234 5 678
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("Hello\r\nGood Bye\r\n".getBytes(StandardCharsets.UTF_8));

        byte [] buffer = new byte[512];
        int [] totalBytesRead = new int[1];
        totalBytesRead[0] = -1;
        int offset = 0;

        int position = InputStreamUtils.readLine(inputStream, buffer, offset, totalBytesRead);

        int index = position+1;

        final String line1 = new String(buffer, 0, index, StandardCharsets.UTF_8);

        assertEquals("Hello\r\n", line1);

        int endOffset = ByteUtils.skipUntilCRLF(buffer, index);

        System.out.println(endOffset);

        final String line2 = new String(buffer, index, endOffset - index, StandardCharsets.UTF_8);

        System.out.println(line2);

        assertEquals("Good Bye\r\n", line2);

    }
}