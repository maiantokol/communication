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
import java.util.ArrayList;

public class WriteRequsetPacket {

    private static final String FILES_DIRECTORY =  "C:\\Users\\idozA\\Desktop\\spl3\\communication\\Skeleton-2\\server\\Flies";

    public static byte[]  handleWriteAndGetResponse(byte[] message, boolean isLoggedIn, State state)
    {
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }

        String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);

        Path filePath = Paths.get(FILES_DIRECTORY, filename);


        if (Files.exists(filePath))
        {
            return ErrorPacket.createErrorResponse((byte) 1, "File already exists");
        }
        state.wrq= true;
        state.dataBlocks = new ArrayList<>();
        state.wrqFilename = filename;
        state.wrqFilepath = String.valueOf(filePath);
        return AckPacket.getAckPacket((short)0); // send ack for block 0
}


}