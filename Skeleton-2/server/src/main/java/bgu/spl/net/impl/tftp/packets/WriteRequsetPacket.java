package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpProtocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WriteRequsetPacket {
    static String filename; //static????
    private static final String FILES_DIRECTORY = "/path/to/your/files/";

    public static byte[]  handleWriteAndGetResponse(byte[] message, boolean isLoggedIn) 
    {
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }

        //to chceck if the file already exists
        Path filePath = Paths.get(FILES_DIRECTORY, filename);
        if (Files.exists(filePath)) 
        {
            return ErrorPacket.createErrorResponse((byte) 1, "File already exists");
        }
         filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
         
        return AckPacket.getAckPacket((short)0); // send ack for block 0  
    
}

public static String getName()
{
    return filename;
}


}