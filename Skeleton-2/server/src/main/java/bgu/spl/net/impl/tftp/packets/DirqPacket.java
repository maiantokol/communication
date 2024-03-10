import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DirqPacket {
    private static final int DATA_SIZE = 512; // TFTP Data block size
    public static FileTransferSession session;
    private static final short OPCODE = 6;
    private static final String FILES_DIRECTORY = "/Users/maiantokol/Library/Mobile Documents/com~apple~CloudDocs/Downloads/לימודים/שנה ב/spl/project3/Skeleton-2/server/Flies"; 

    public static List<byte[]> createDirqResponse(boolean isLoggedIn) {
        if(!isLoggedIn)
        {
            List<byte[]> errorList = new ArrayList<>();
            errorList.add(ErrorPacket.createErrorResponse((byte)6,"User not logged in")) ;
            return errorList;
        }

        File folder = new File(FILES_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) { //TODO: check if need to return error packet
            List<byte[]> errorList = new ArrayList<>();
            errorList.add(ErrorPacket.createErrorResponse((byte)6,"dont have any files")) ;
            return errorList; // or handle the error appropriately
        }

        // Calculate the size of the buffer needed
        int totalSize = 6; // start with the size for the opcode and the block number and totalSize
        for (File file : listOfFiles) {
            totalSize += file.getName().getBytes(StandardCharsets.UTF_8).length + 1; // filename length + null terminator
        }
/*
 *  ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putShort(OPCODE); // add opcode
        buffer.putShort((short)totalSize); // add totalSize
        buffer.putShort((short)1); // TODO: check about the block number??
         // add filenames
        for (File file : listOfFiles) {
            buffer.put(file.getName().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) 0); // null terminator for the string
        }
 */
       

         List<byte[]> packets = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(DATA_SIZE);
        buffer.putShort(OPCODE); // Opcode for DIRQ
        buffer.putShort((short) 0); // Placeholder for block number, to be set later

        for (File file : listOfFiles) {
            byte[] fileNameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
            if (buffer.position() + fileNameBytes.length + 1 > buffer.capacity()) {
                // Finish the current packet and start a new one
                packets.add(finishPacket(buffer, packets.size() + 1)); // Block number is index + 1
                buffer.clear();
                buffer.putShort(OPCODE);
                buffer.putShort((short) 0); // Placeholder for block number
            }

            buffer.put(fileNameBytes);
            buffer.put((byte) 0); // Null terminator for the filename
        }

        if (buffer.position() > 4) { // If there's any data beyond the opcode and block number
            packets.add(finishPacket(buffer, packets.size() + 1)); // Final packet
        }

        return packets;
        //return buffer.array();
    }
    

    private static byte[] finishPacket(ByteBuffer buffer, int blockNumber) {
        int size = buffer.position();
        buffer.flip();
        buffer.putShort(2, (short) blockNumber); // Set the correct block number
        byte[] packet = new byte[size];
        buffer.get(packet);
        return packet;
    }

   
}
