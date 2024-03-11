package bgu.spl.net.impl.tftp.packets;

import java.nio.charset.StandardCharsets;

public class ErrorPacket {

        public static byte[] createErrorResponse(byte errorCode, String errorMessage) {
            System.out.println("[createErrorResponse] errorMessage is: "+errorMessage);
            byte[] errorMessageBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            byte[] packet = new byte[4 + errorMessageBytes.length + 1];
            packet[0] = 0;
            packet[1] = 5;
            packet[2] = 0;
            packet[3] = errorCode;
            // Copy the error message bytes into the packet
            System.arraycopy(errorMessageBytes, 0, packet, 4, errorMessageBytes.length);
            // Add the zero byte to terminate the error message string
            packet[packet.length - 1] = 0;
            return packet;
        }

}
