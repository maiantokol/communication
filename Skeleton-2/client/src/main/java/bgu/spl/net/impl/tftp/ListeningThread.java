package bgu.spl.net.impl.tftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static bgu.spl.net.impl.tftp.TftpClientEncoderDecoder.printBytesInShortFormat;

public class ListeningThread extends Thread {
   // private final Socket socket;
    private final TftpClientProtocol protocol; // Your protocol class
    private final TftpClientEncoderDecoder encoderDecoder; // Your encoder/decoder class

    private DataInputStream in;
    private DataOutputStream out;


    public ListeningThread(DataInputStream in, DataOutputStream out, TftpClientProtocol protocol, TftpClientEncoderDecoder encdec) {
        this.in = in;
        this.out = out;
        this.protocol = protocol;
        this.encoderDecoder = encdec;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Here we assume you have methods in your encoder/decoder to handle stream input
                byte[] packet = this.encoderDecoder.decodeNextByte(in.readByte());

                // The protocol processes the received packet and possibly sends responses
                if (packet != null) {
                    byte[] response= protocol.process(packet);
                    //System.out.println ("response from protocol: ");

                    if (response != null) {
                        out.write(response);
                        out.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error in ListeningThread: " + e.getMessage());
        }
    }
}

