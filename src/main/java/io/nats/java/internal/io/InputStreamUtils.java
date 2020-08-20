package io.nats.java.internal.io;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {

    private static final int CR = '\r';
    private static final int NL = '\n';

    public static int read(final InputStream stream, byte[] buffer, int offset,  int[] position) throws IOException {

        int lastByteRead = 0;

        if (offset > 0) {
            lastByteRead = buffer[offset - 1];
        }
        if (lastByteRead == CR) {
            int read = stream.read();
            buffer[offset] = (byte) read;
            if (read == NL) {
                position[0] = offset+1;
                return 1;
            } else {
                return read(stream, buffer, offset + 1, position) + 1;
            }
        }


        int bytesRead = stream.read(buffer, offset, buffer.length-offset);
        if (bytesRead == -1) {
            position [0] =-2;
            return 0;
        }

        // Look for \r\n
        boolean foundCR = false;

        loop:
        for (int index = offset; index < bytesRead; index++) {

            switch (buffer[index]) {
                case CR:
                    foundCR = true;
                    break;
                case NL:
                    if (foundCR) {
                        position[0] = index;
                        System.out.println(position[0]);
                        break loop;
                    }
                    break;
                default:
                    foundCR = false;
            }
        }

        return bytesRead;

    }


    public static int readLine(final InputStream stream, byte[] buffer, int offset, int [] read  ) throws IOException {

        int [] position  = new int[1];
        position[0] = -1;
        while (position[0] == -1) {
            read[0] += read(stream, buffer, offset, position);
        }

        return position[0];
    }
}
