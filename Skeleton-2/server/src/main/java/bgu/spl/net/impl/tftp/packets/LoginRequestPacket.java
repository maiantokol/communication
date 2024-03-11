package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpProtocol;

import java.nio.charset.StandardCharsets;

public class LoginRequestPacket {
    public static byte[]  handleLoginAndGetResponse(byte[] message) {
        String username = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
        System.out.println("[handleLoginAndGetResponse] username is: "+username);
        if(TftpProtocol.connectedUsers.contains(username)){
            String errorMessage = "user already logged in";
            byte errorCode = 7;
            return ErrorPacket.createErrorResponse(errorCode,errorMessage);
        }
        TftpProtocol.connectedUsers.add(username);
        System.out.println("[handleLoginAndGetResponse] connectedUsers is: "+TftpProtocol.connectedUsers.toString());

        return AckPacket.getAckPacket((short)0); // send ack for block 0
    }
}
