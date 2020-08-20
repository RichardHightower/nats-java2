package io.nats.java.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ByteUtils {

    private static final int CR = '\r';
    private static final int NL = '\n';

    public static int skipUntilEndQuote(byte[] bytes, int start) {

        for (int index = start; index < bytes.length; index++) {
            byte b = bytes[index];
            if (b == '\'') {
                return index;
            }
        }

        return start;
    }

    public static int skipWhiteSpace(byte[] bytes, int start) {

        loop:
        for (int index = start; index < bytes.length; index++) {
            byte b = bytes[index];
            switch (b) {
                case ' ':
                case '\t':
                    continue loop;
                default:
                    return index;
            }
        }

        return start;
    }

    public static int skipUntilCRLF(byte[] buffer, int start) {

        boolean foundCR = false;
        int position = -1;

        loop:
        for (int index = start; index < buffer.length; index++) {

            switch (buffer[index]) {
                case CR:
                    foundCR = true;
                    break;
                case NL:
                    if (foundCR) {
                        position = index + 1;
                        break loop;
                    }
                    break;
                default:
                    foundCR = false;
            }
        }

        return position;

    }


    public static int skipWhiteSpaceAndFirstQuote(byte[] bytes, int start) {
        byte b = -1;
        int index = 0;
        loop:
        for (index = start; index < bytes.length; index++) {
            b = bytes[index];
            switch (b) {
                case ' ':
                case '\t':
                    break;
                case '\'':
                    break loop;
                //default:
                //    break;
            }
        }

        if (b == '\'') {
            index++;
            return index;
        } else {
            throw new IllegalStateException("Quote not found");
        }
    }

    public static int skipUntilWhiteSpace(byte[] bytes, int start) {

        for (int index = start; index < bytes.length; index++) {
            byte b = bytes[index];
            switch (b) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    return index;
            }
        }
        return start;
    }

    public static String readStringDelimByWhitespace(byte[] bytes, int start, int[] pointer) {

        final int startSubjectIndex = ByteUtils.skipWhiteSpace(bytes, start);
        final int endSubjectIndex = ByteUtils.skipUntilWhiteSpace(bytes, startSubjectIndex);
        pointer[0] = endSubjectIndex;
        return new String(bytes, startSubjectIndex, endSubjectIndex - startSubjectIndex, StandardCharsets.UTF_8);
    }

    public static String readErrorString(byte[] bytes, int start) {

        final int startIndex = skipWhiteSpaceAndFirstQuote(bytes, start);

        final int endIndex = ByteUtils.skipUntilEndQuote(bytes, startIndex);

        return new String(bytes, startIndex, endIndex - startIndex, StandardCharsets.UTF_8);
    }

    public static int readSizeFromMsgAndValidate(byte[] buffer, int[] location, int highWaterMark, int[] indexes, int numFeatures[]) throws IOException {


        if (highWaterMark < 13) {
            //012345678 9 0 1 2
            //MSG s s s\r\n\r\n
            throw new IOException("Message needs a least 13 bytes");
        }

        int index = location[0];
        int startIndex = index;

        if ((buffer[index] == 'm' || buffer[index] == 'M') &&
                (buffer[index + 1] == 's' || buffer[index + 1] == 'S') &&
                (buffer[index + 2] == 'g' || buffer[index + 2] == 'G')) {

            index += 3;

            try {
                index = ByteUtils.skipWhiteSpace(buffer, index);
                indexes[0] = index - startIndex;
                index = ByteUtils.skipUntilWhiteSpace(buffer, index);
                indexes[1] = index - startIndex;

                index = ByteUtils.skipWhiteSpace(buffer, index);
                indexes[2] = index - startIndex;
                index = ByteUtils.skipUntilWhiteSpace(buffer, index);
                indexes[3] = index - startIndex;

                int startIndexOfSize = ByteUtils.skipWhiteSpace(buffer, index);
                int endIndexOfSize = ByteUtils.skipUntilWhiteSpace(buffer, startIndexOfSize);
                indexes[4] = startIndexOfSize - startIndex;
                indexes[5] = endIndexOfSize - startIndex;
                index = endIndexOfSize;


                if (buffer[endIndexOfSize + 1] != '\r' && buffer[endIndexOfSize + 2] != '\n') {


                    startIndexOfSize = ByteUtils.skipWhiteSpace(buffer, index);
                    endIndexOfSize = ByteUtils.skipUntilWhiteSpace(buffer, startIndexOfSize);
                    indexes[6] = startIndexOfSize - startIndex;
                    indexes[7] = endIndexOfSize - startIndex;
                    numFeatures[0] = 5;
                } else {
                    numFeatures[0] = 4;
                }

                int locationOfPayLoad = endIndexOfSize + 2;
                location[0] = locationOfPayLoad;
                indexes[8] = locationOfPayLoad;

                int size = Integer.parseInt(new String(buffer, startIndexOfSize, endIndexOfSize - startIndexOfSize, StandardCharsets.UTF_8));
                indexes[9] = locationOfPayLoad + size;
                return size;
            } catch (Exception ex) {
                throw new IOException("Unable to parse size", ex);
            }
        } else {
            throw new IOException("Unable to validate MSG protocol or read size");
        }
    }

}

