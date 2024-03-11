package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.packets.AckPacket;
import bgu.spl.net.impl.tftp.packets.ErrorPacket;

import java.nio.ByteBuffer;

public class DiscPacket {


    public static byte[] createDiscPacket(boolean isLoggedIn) {
        if (!isLoggedIn) {
            return ErrorPacket.createErrorResponse((byte) 6, "User not logged in");
        }
        return AckPacket.getAckPacket((short)0); // send ack for block 0

    }

}
