package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.packets.ErrorPacket;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DirqPacket {

    private static final short OPCODE = 6;
    private static final String FILES_DIRECTORY = "C:\\Users\\user\\Desktop\\communication\\Skeleton-2\\server\\Flies";

    public static byte[] createDirqResponse(boolean isLoggedIn) {
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }

        File folder = new File(FILES_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            return new byte[0]; // or handle the error appropriately
        }

        // Calculate the size of the buffer needed
        int totalSize = 6; // start with the size for the opcode and the block number and totalSize
        for (File file : listOfFiles) {
            totalSize += file.getName().getBytes(StandardCharsets.UTF_8).length + 1; // filename length + null terminator
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putShort(OPCODE); // add opcode
        buffer.putShort((short)totalSize); // add totalSize
        buffer.putShort((short)1); // TODO: check about the block number??

        // add filenames
        for (File file : listOfFiles) {
            buffer.put(file.getName().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // null terminator for the string
        }

        return buffer.array();
    }
}
