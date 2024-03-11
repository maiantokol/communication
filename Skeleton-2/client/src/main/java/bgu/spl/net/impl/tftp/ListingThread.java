package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.net.Socket;

public class ListeningThread extends Thread {
    private final Socket socket;
    private final TftpClientProtocol protocol; // Your protocol class
    private final TftpClientEncoderDecoder encoderDecoder; // Your encoder/decoder class

    public ListeningThread(Socket socket, TftpClientProtocol protocol, TftpClientEncoderDecoder encoderDecoder) {
        this.socket = socket;
        this.protocol = protocol;
        this.encoderDecoder = encoderDecoder;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                // Here we assume you have methods in your encoder/decoder to handle stream input
                byte[] packet = encoderDecoder.decodeNextByte(socket.getInputStream());

                // The protocol processes the received packet and possibly sends responses
                protocol.process(packet);
            }
        } catch (IOException e) {
            System.out.println("Error in ListeningThread: " + e.getMessage());
        }
    }
}

