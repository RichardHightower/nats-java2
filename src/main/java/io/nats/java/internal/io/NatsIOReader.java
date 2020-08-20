package io.nats.java.internal.io;

import io.nats.java.internal.ByteUtils;
import io.nats.java.internal.NATSProtocolVerb;
import io.nats.java.internal.ServerMessage;
import io.nats.java.internal.actions.OutputQueue;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class NatsIOReader {

    private final byte[] buffer = new byte[1024 * 64];


    private final InputStream inputStream;
    private final Socket clientSocket;
    private final OutputQueue<ServerMessage> serverMessages;
    private int highWaterMark = 0;
    private int positionOfEndOfLine;
    private int startOfBuffer;


    public NatsIOReader(InputStream inputStream, Socket clientSocket, OutputQueue<ServerMessage> serverMessages) {
        this.inputStream = inputStream;
        this.clientSocket = clientSocket;
        this.serverMessages = serverMessages;
    }

    public void run() throws IOException {


        int[] bytesReadHolder = new int[1];
        int bytesRead;

        while (!clientSocket.isClosed()) {

            for (int i = 0; i < 10; i++) {

                bytesReadHolder[0] = -1;
                startOfBuffer = 0;
                positionOfEndOfLine = InputStreamUtils.readLine(inputStream, buffer, highWaterMark, bytesReadHolder);
                bytesRead = bytesReadHolder[0];

                if (bytesRead == -1) {
                    //Do something here.
                } else {
                    highWaterMark += bytesRead;
                }

                byte b = buffer[0];

                switch (b) {
                    case 'I':
                    case 'i':
                        processInfo();
                        break;
                    case 'M':
                    case 'm':
                        processMessage();
                        break;
                    case 'P':
                    case 'p':
                        processPingPong();
                        break;
                    case '+':
                        processOK();
                        break;
                    case '-':
                        processError();
                        break;

                    default:
                        throw new IOException("Illegal message char at start of message from server '" + (char) b + "'");
                }

            }


        }

    }

    private void processError() throws IOException {

    }

    private void processOK() throws IOException {

    }

    private void processPingPong() throws IOException {

    }

    private void processMessage() throws IOException {


        int[] indexes = new int[8];
        int[] locationIndex = new int[1];
        int[] numFeatures = new int[1];
        locationIndex[0] = startOfBuffer;
        int size = ByteUtils.readSizeFromMsgAndValidate(buffer, locationIndex, highWaterMark, indexes, numFeatures);
        int index = locationIndex[0];

        ServerMessage serverMessage = null;

        int protocolSize = index - startOfBuffer + 2;
        int totalSizeWithMsg = protocolSize + size;
        //int availableSpaceInBuffer = highWaterMark - (startOfBuffer + protocolSize);

        //Wont fit into buffer.
        if (buffer.length - index < size) {
            serverMessage = getServerMessage(indexes, numFeatures[0], totalSizeWithMsg, protocolSize);

            //Will fit into buffer but we have not read in enough.
        } else if (totalSizeWithMsg - (protocolSize + startOfBuffer) > size) {
            int bytesRead;
            do {
                bytesRead = inputStream.read(buffer);
                if (bytesRead > 0) {
                    index += bytesRead;
                }
            } while (bytesRead != -1 && bytesRead != 0);


            byte b[] = new byte[totalSizeWithMsg];
            System.arraycopy(buffer, startOfBuffer, b, 0, totalSizeWithMsg);
            serverMessage = new ServerMessage(b, NATSProtocolVerb.MESSAGE, indexes, numFeatures[0]);

        } else {
            //We already read the whole thing.
            byte b[] = new byte[totalSizeWithMsg];
            System.arraycopy(buffer, startOfBuffer, b, 0, totalSizeWithMsg);
            serverMessage = new ServerMessage(b, NATSProtocolVerb.MESSAGE, indexes, numFeatures[0]);

            final int end = startOfBuffer + totalSizeWithMsg;

            //We read in the whole message and none of the next message.
            if (buffer[end-1] == '\n' && buffer[end-2] == '\r') {
                startOfBuffer = 0;
            } else {
                //We read in some of the next message.
                startOfBuffer = end;
            }

        }

        System.out.println(serverMessage);
        // TODO left off here

    }

    private ServerMessage getServerMessage(int[] indexes, int numFeature, int totalSizeWithMsg, int protocolSize) throws IOException {


        final ByteBuffer byteBuffer = ByteBuffer.allocate(totalSizeWithMsg);
        ServerMessage serverMessage;

        byteBuffer.put(buffer, startOfBuffer, protocolSize);
        int bytesRead;
        do {
            bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                byteBuffer.put(buffer, 0, bytesRead);
            }
        } while (bytesRead != -1 && bytesRead != 0);
        byte[] b = new byte[byteBuffer.remaining()];
        byteBuffer.get(b);
        serverMessage = new ServerMessage(b, NATSProtocolVerb.MESSAGE, indexes, numFeature);
        return serverMessage;
    }

    private void processInfo() throws IOException {

    }
}
