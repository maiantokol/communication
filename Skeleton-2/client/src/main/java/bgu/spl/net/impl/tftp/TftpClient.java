package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TftpClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    // Assuming TftpClientEncoderDecoder is similar to TftpEncoderDecoder but adapted for client
    private TftpClientEncoderDecoder encoderDecoder;

    public TftpClient(String serverIp, int serverPort) throws Exception {
        this.serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
        this.socket = new DatagramSocket();
        this.encoderDecoder = new TftpClientEncoderDecoder();
    }

    public void send(String message) throws IOException {
        byte[] bytesToSend = encoderDecoder.encode(message.getBytes());
        DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public void receive() throws IOException {
        byte[] buffer = new byte[516]; // TFTP max packet size
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        // Process received packet...
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        TftpClient client = new TftpClient("localhost", 7777);
// open socket
        Thread keybordThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null && !line.equalsIgnoreCase("quit")) {
                    client.send(line); // This needs to be adapted for your TFTP operations
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        });

        Thread ListeningThread = new Thread(() -> {
            try {
                while (true) { // Could also use a condition to allow stopping the thread
                    client.receive(); // Implement to process received data accordingly
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        keybordThread.start();
        ListeningThread.start();
    }
}