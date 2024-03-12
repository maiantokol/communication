package bgu.spl.net.impl.tftp;
import java.io.File;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.*;
import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private int numOfBytes = 0;
    private boolean isOpcodeComplete = false;
    private ByteBuffer lengthBuffer = ByteBuffer.wrap(new byte[2]);
    @Override
    public byte[] decodeNextByte(byte nextByte) { //who calls this function???? the server?
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison

       // System.out.println("[decodeNextByte] got byte: "+(short)nextByte);
        numOfBytes++;
        pushByte(nextByte);
        if (numOfBytes > 2 && isCompletePacket())
        {
            return popPacket();
        }
        return null;
    }
    @Override
    public byte[] encode(byte[] message)
    {
        return message;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }
    private byte[] popPacket() //to fix
    {
        byte[] packet = Arrays.copyOf(bytes, len);
        len = 0;
        return packet;
    }
    private boolean isCompletePacket()
    {
        short opcode = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0,2)).order(ByteOrder.BIG_ENDIAN).getShort();

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

    // Common method to decode a UTF-8 string terminated by a zero byte
    private String decodeString(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        byte nextByte;
        while ((nextByte = buffer.get()) != 0) {
            builder.append((char) nextByte);
        }
        return builder.toString();
    }

    public static void printBytesInShortFormat(byte[] message) {
        System.out.print("byte array is: ");
        for (byte b : message) {
            System.out.print((short)b);
            System.out.print(", ");
        }
        System.out.println(); // Move to the next line after printing all bytes
    }

    public static void printFirst10BytesInShortFormat(byte[] message) {
        for (int i = 0; i < message.length && i < 10; i++) {
            System.out.print((short)message[i]);
            System.out.print(", ");
        }
        System.out.println(); // Move to the next line after printing
    }
}