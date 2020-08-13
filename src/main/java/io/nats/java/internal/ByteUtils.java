package io.nats.java.internal;

import java.nio.charset.StandardCharsets;

public class ByteUtils {

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

    public static int skipWhiteSpaceAndFirstQuote(byte[] bytes, int start) {
        byte b = -1;
        int index = 0;
        loop:
        for ( index = start; index < bytes.length; index++) {
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

    public static String readStringDelimByWhitespace(byte[] bytes, int start, int [] pointer) {

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
}
