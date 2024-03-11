
import bgu.spl.net.impl.tftp.TftpClientEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpClientProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class KeyboardThread extends Thread {
    private final Socket socket;
    private final BufferedReader keyboard;
    private final TftpClientProtocol protocol; // Your protocol class
    private final TftpClientEncoderDecoder encoderDecoder; // Your encoder/decoder class

    public KeyboardThread(Socket socket, TftpClientProtocol protocol, TftpClientEncoderDecoder encoderDecoder) throws IOException {
        this.socket = socket;
        this.keyboard = new BufferedReader(new InputStreamReader(System.in));
        this.protocol = protocol;
        this.encoderDecoder = encoderDecoder;
    }

    @Override
    public void run() {
        try {
            String userInput;
            while (!socket.isClosed() && (userInput = keyboard.readLine()) != null) {
                // The protocol could generate the appropriate packet based on user input
                byte[] packet = userInput.getBytes(StandardCharsets.UTF_8); //TOCHECK
                if (packet != null) {
                    socket.getOutputStream().write(encoderDecoder.encode(packet));
                    socket.getOutputStream().flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Error in KeyboardThread: " + e.getMessage());
        } finally {
            try {
                keyboard.close();
            } catch (IOException e) {
                // Ignore as we are closing the thread
            }
        }
    }
}
