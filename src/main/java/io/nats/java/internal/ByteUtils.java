package io.nats.java.internal;

import java.nio.charset.StandardCharsets;

public class ByteUtils {

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
}
