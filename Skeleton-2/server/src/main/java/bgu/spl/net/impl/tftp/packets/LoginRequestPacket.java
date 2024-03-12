package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpProtocol;

import java.nio.charset.StandardCharsets;

public class LoginRequestPacket {
    public static byte[]  handleLoginAndGetResponse(byte[] message, int userid) {
        String username = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
        if(TftpProtocol.US.contains(username))
        {
            String errorMessage = "user already logged in";
            byte errorCode = 7;
            return ErrorPacket.createErrorResponse(errorCode,errorMessage);
        }
       // int userid=TftpProtocol.getID();
        TftpProtocol.US.addUser(username,userid);

        return AckPacket.getAckPacket((short)0); // send ack for block 0
    }
}