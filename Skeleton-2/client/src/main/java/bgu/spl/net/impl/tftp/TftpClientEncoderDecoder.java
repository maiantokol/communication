package bgu.spl.net.impl.tftp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpClientEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[1 << 10]; // Start with 1k
    private int len = 0;

    private int numOfBytes = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
       // System.out.println("got byte "+nextByte);


        numOfBytes++;
        pushByte(nextByte);
      //  printFirst10BytesInShortFormat(bytes);
        if (numOfBytes > 2 && isCompletePacketClient())
        {
            return popPacket();
        }
        return null;
    }

        public byte[] encode(byte[] message) {

        String[] parts = new String(message).split(" ", 2);
        String command = parts[0].toUpperCase();
        String argument = parts.length > 1 ? parts[1] : null;

        switch (command) {
            case "LOGRQ":
                return createLogRqPacket(argument);
            case "DELRQ":
                return createDelRqPacket(argument);
            case "RRQ":
                StateClient.fileName=argument; //how to fix
                return createRrqPacket(argument);
            case "WRQ":
                return createWrqPacket(argument);
            case "DIRQ":
                return createDIRQPacket();
            default:
                // Handle unknown command
                System.err.println("Unknown command: " + command);
                return null;
        }
    }

        private byte[] createLogRqPacket(String username) {

            ByteBuffer buffer = ByteBuffer.allocate(2 + username.getBytes(StandardCharsets.UTF_8).length + 1);
            buffer.putShort((short) 7); // Opcode for LOGRQ is 7
            buffer.put(username.getBytes(StandardCharsets.UTF_8)); // Put the username
            buffer.put((byte) 0); // Add zero byte to terminate the string
            return buffer.array();
        }

        private byte[] createDelRqPacket(String filename) {

            ByteBuffer buffer = ByteBuffer.allocate(2 + filename.getBytes(StandardCharsets.UTF_8).length + 1);
            buffer.putShort((short) 8); // Opcode for DELRQ is 8
            buffer.put(filename.getBytes(StandardCharsets.UTF_8)); // Put the filename
            buffer.put((byte) 0); // Add zero byte to terminate the string
            return buffer.array();

        }
    public static byte[] createDISCPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(2); // Only need 2 bytes for the opcode
        buffer.putShort((short) 10); // Opcode for DISC is 10
        return buffer.array();
    }

        private byte[] createRrqPacket(String filename) {
            return createRequestPacket((short) 1, filename);

        }

        private byte[] createWrqPacket(String filename) {
            return createRequestPacket((short) 2, filename);

        }
    public static byte[] createACKPacket(short blockNumber) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort((short) 4); // Opcode for ACK
        buffer.putShort(blockNumber);
        return buffer.array();
    }


    public static byte[] createDIRQPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(2); // Allocate buffer for 2 bytes
        short opcode = 6; // DIRQ opcode is 6
        buffer.putShort(opcode); // Put the opcode in the buffer
        return buffer.array(); // Convert the buffer to a byte array
    }



    public static byte[] createDataPacket(short blockNumber, byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putShort((short) 3);
        buffer.putShort(blockNumber);
        buffer.put(data);
        return buffer.array();

    }
    private static byte[] createRequestPacket(short opcode, String filename) {
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(2 + filenameBytes.length + 1);
        buffer.putShort(opcode);
        buffer.put(filenameBytes);
        buffer.put((byte) 0); // Null terminator for the string
        return buffer.array();
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = java.util.Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private byte[] popPacket() {
        byte[] packet = java.util.Arrays.copyOf(bytes, len);
        len = 0; // Reset length after popping
        return packet;
    }

    private boolean isCompletePacket() {
        // Simplified example: let's assume every packet ends with a zero byte
        // Adjust this method based on your specific protocol needs
        return len > 1 && bytes[len - 1] == 0;
    }


    private boolean isCompletePacketClient()
    {
        short opcode = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0,2)).order(ByteOrder.BIG_ENDIAN).getShort();
        //System.out.println("[isCompletePacket] opcode is: "+opcode);
        switch (opcode) {
            case 1: // RRQ
            case 2: // WRQ
            case 7: // LOGRQ
            case 8: // DELRQ
                return len > 2 && bytes[len-1] == 0;

            case 3: // DATA
                if (len < 4) return false;
                short packetSize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 2, 4)).order(ByteOrder.BIG_ENDIAN).getShort();
                // DATA packet length = 2 bytes opcode + 2 bytes packet size + 2 bytes block number + packet size
                return len == 4 + 2 + packetSize;

            case 4: // ACK packet
                // ACK packet  2 bytes for the opcode + 2 bytes for the block number
                return len == 4;

            case 5: // ERROR packet
                // ERROR packets have a 4-byte header (opcode + error code) followed by an error message terminated with zero
                return len > 4 && bytes[len - 1] == 0;

            case 6: // DIRQ
            case 10: // DISC
                // These packets are  2 bytes .
                return len == 2;

            case 9: // BCAST
                return len>3 & bytes[len-1]==0;
            // These packets end with a zero .

            default:
                return false;
        }
    }


}



/*

public class TftpEncoderDecoder {

    // Temporary storage for accumulating bytes from the network
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public TftpPacket decodeNextPacket(InputStream in) throws IOException {
        // Reset buffer for next packet
        buffer.clear();

        // Read the opcode, assuming it's 2 bytes
        if (in.read(buffer.array(), 0, 2) != 2) {
            throw new IOException("Failed to read opcode from the stream.");
        }
        buffer.limit(2); // We've read 2 bytes

        // Convert the first 2 bytes to the opcode
        short opcode = buffer.getShort(0);

        // Depending on the opcode, read the rest of the packet
        // This is a simplified example. Your implementation will need to
        // dynamically determine how many more bytes to read based on the
        // packet type and possibly the contents of the packet itself.
        switch (opcode) {
            case 1: // Example opcode for RRQ
                // Read the rest of the RRQ packet
                // You'll need to define how to parse the specific packet types
                return decodeRRQPacket(in);
            case 2: // Example opcode for WRQ
                return decodeWRQPacket(in);
            // Add cases for other opcodes
            default:
                throw new IOException("Unknown opcode: " + opcode);
        }
    }

    private TftpPacket decodeRRQPacket(InputStream in) throws IOException {
        // Implementation for decoding an RRQ packet
        // This will likely involve additional reads from the InputStream `in`
        // and constructing an appropriate packet object for the RRQ
        return new TftpRRQPacket(); // Placeholder
    }

    private TftpPacket decodeWRQPacket(InputStream in) throws IOException {
        // Similarly, implement decoding for a WRQ packet
        return new TftpWRQPacket(); // Placeholder
    }

    // Define TftpPacket and subclasses like TftpRRQPacket, TftpWRQPacket, etc.
}



 */