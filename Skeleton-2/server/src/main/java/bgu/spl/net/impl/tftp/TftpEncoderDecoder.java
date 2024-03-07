package bgu.spl.net.impl.tftp;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.*;
import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder


    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
       //private ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
    private boolean isOpcodeComplete = false;
    private ByteBuffer lengthBuffer = ByteBuffer.wrap(new byte[2]);
    @Override
    public byte[] decodeNextByte(byte nextByte) { //who calls this function???? the server?
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (!isOpcodeComplete) 
        {
            lengthBuffer.put(nextByte);
            isOpcodeComplete = true;
            lengthBuffer.flip();

        }
        if (isCompletePacket()) 
        {
            return popPacket(); 
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(byte[] message) 
    {
        return (message + "0").getBytes(); //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private byte[] popPacket() //to fix
    {
       
       String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
       byte[] byteArray = result.getBytes(StandardCharsets.UTF_8);

       len = 0;
       return byteArray;
   }




    
 private boolean isCompletePacket()
 {
    short opcode =ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0,2)).order(ByteOrder.BIG_ENDIAN).getShort();
    switch (opcode) {
        case 1: // RRQ
        case 2: // WRQ
        case 7: // LOGRQ
        case 8: // DELRQ
            // These packets end with a zero byte. Check if the last byte is zero.
            return len > 2 && bytes[len - 1] == 0;

        case 3: // DATA
            if (len < 4) return false;
            // Get the packet size, which is in the third and fourth bytes of the DATA packet
            short packetSize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 2, 4)).order(ByteOrder.BIG_ENDIAN).getShort();
            // Check if the length of the packet including the header matches the length read
            // DATA packet length = 2 bytes opcode + 2 bytes packet size + 2 bytes block number + packet size
            return len == 4 + 2 + packetSize;

        case 4: // ACK packet
            // ACK packets are always 4 bytes: 2 bytes for the opcode + 2 bytes for the block number
            return len == 4;

        case 5: // ERROR packet
            // ERROR packets have a 4-byte header (opcode + error code) followed by an error message terminated with zero
            // We need to check if the last byte is zero to determine the end of the packet
            return len > 4 && bytes[len - 1] == 0;

        case 6: // DIRQ
        case 10: // DISC
            // These packets are always 2 bytes long.
            return len == 2;

        case 9: // BCAST
        return len>3 & bytes[len-1]==0;
            // These packets end with a zero byte.

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
}







