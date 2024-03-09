package bgu.spl.net.impl.tftp.packets;

public class AckPacket {
    public static byte[] getAckPacket(short blockNumber) {
        byte[] ackPacket = new byte[4];

        // Opcode for ACK is 4
        ackPacket[0] = 0;
        ackPacket[1] = 4;

        // split the block number into two bytes and put it into the ackPacket
        ackPacket[2] = (byte) (blockNumber >> 8); // high byte of block number
        ackPacket[3] = (byte) (blockNumber);      // low byte of block number
        return ackPacket;
    }
}