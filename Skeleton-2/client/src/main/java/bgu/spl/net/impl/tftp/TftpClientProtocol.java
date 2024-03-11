package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TftpClientProtocol {

    private boolean isLoggedIn;
    private TftpClientEncoderDecoder encoderDecoder; // Assuming you have this class implemented
    private TftpClient client; // This should be your client class that can send and receive packets

    public TftpClientProtocol(TftpClientEncoderDecoder encoderDecoder, TftpClient client) {
        this.isLoggedIn = false;
        this.encoderDecoder = encoderDecoder;
        this.client = client;
    }

    public void process(byte[] message) {
        short opcode = getOpCode(message);

        switch (opcode) {
            case 1: // DATA packet
                // Handle DATA packet, save data and send ACK
                handleRRQPacket(message);
                break;
            case 3: // DATA packet
                // Handle DATA packet, save data and send ACK
                handleDataPacket(message);
                break;

            case 4: // ACK packet
                // Print ACK message
                handleAckPacket(message);
                break;

            case 5: // ERROR packet
                // Handle error, print error message
                handleErrorPacket(message);
                break;

            case 9: // BCAST packet
                // Print BCAST message
                handleBcastPacket(message);
                break;

            // Add more cases if needed

            default:
                System.out.println("Received unknown opcode: " + opcode);
                break;
        }
    }

    private void handleRRQPacket(byte[] message) {
        // Extract data and save to file or buffer based on your application's need
        String filename = new String(message, 3, message.length - 4);
        System.out.println("RRQ " + filename +"complete");
        // Send ACK for DATA packet here...
    }

    private void handleDataPacket(byte[] message) {
        // Extract data and save to file or buffer based on your application's need
        System.out.println("Received DATA packet");
        // Send ACK for DATA packet here...
    }

    private void handleAckPacket(byte[] message) {
        // Extract block number and print ACK message
        ByteBuffer wrap = ByteBuffer.wrap(message).order(ByteOrder.BIG_ENDIAN);
        short blockNumber = wrap.getShort(2);
        System.out.println("ACK " + blockNumber);
    }

    private void handleErrorPacket(byte[] message) {
        // Print error message
        String errorMessage = new String(message, 4, message.length - 5);
        System.out.println("Error: " + errorMessage);
    }

    private void handleBcastPacket(byte[] message) {
        // Print BCAST message
        byte addOrDel = message[2];
        String status = (addOrDel == 1) ? "added" : "deleted";
        String filename = new String(message, 3, message.length - 4);
        System.out.println("BCAST " + status + " " + filename);
    }

    private short getOpCode(byte[] message) {
        return ByteBuffer.wrap(message).order(ByteOrder.BIG_ENDIAN).getShort();
    }
}
