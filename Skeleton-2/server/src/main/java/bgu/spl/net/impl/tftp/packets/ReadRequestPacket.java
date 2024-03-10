/*
 * package bgu.spl.net.impl.tftp.packets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
public class ReadRequestPacket
 {
    
   private static final int DATA_SIZE = 512; // TFTP Data block size
    private static final String FILES_DIRECTORY = "/Users/maiantokol/Library/Mobile Documents/com~apple~CloudDocs/Downloads/לימודים/שנה ב/spl/project3/Skeleton-2/server/Flies";
    private static boolean firstMessage = true; //to check if good
    private static File file;
    public static byte[] handleReadAndGetResponse(byte[] message, boolean isLoggedIn)
     {
        if (!isLoggedIn) {
            return ErrorPacket.createErrorResponse((byte) 6, "User not logged in");
        }
        if (firstMessage)
        {
            String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
            file = new File(FILES_DIRECTORY + filename);
    
            if (!file.exists()) {
                return ErrorPacket.createErrorResponse((byte) 1, "File not found");
            }
            firstMessage=false;
        }
else
{
    try (FileInputStream fis = new FileInputStream(file)) {
        ByteBuffer buffer = ByteBuffer.allocate(DATA_SIZE);
        int bytesRead;

        while ((bytesRead = fis.read(buffer.array())) != -1) { 
            int blockNumber = extractBlockNumberFromAck(message)+1;
            byte[] packet= createDataPacket( blockNumber, buffer.array(), bytesRead);
        
            buffer.clear(); // Clear the buffer for the next read
            return packet;
        }
    } catch (IOException e) {
        e.printStackTrace();
        return ErrorPacket.createErrorResponse((byte) 0, "Error reading file"); //to check
    }
}
    
      

        return null; // This will never be reached
    }

    private static byte[] createDataPacket(int blockNumber, byte[] data, int bytesRead) {
        ByteBuffer dataPacket = ByteBuffer.allocate(6 + bytesRead);
        dataPacket.putShort((short) 3); // Opcode for DATA
        dataPacket.putShort((short) bytesRead); // Data size
        dataPacket.putShort((short) blockNumber); // Block number
        dataPacket.put(data, 0, bytesRead); // File data
        return dataPacket.array();
}

private static int extractBlockNumberFromAck(byte[] ackPacket) {
    // Ensure the ackPacket has at least 4 bytes (opcode + block number)
    if (ackPacket.length < 4) {
        throw new IllegalArgumentException("Invalid ACK packet size.");
    }

    ByteBuffer buffer = ByteBuffer.wrap(ackPacket);
    short opcode = buffer.getShort(); // First 2 bytes are the opcode

    // Ensure this is indeed an ACK packet
    if (opcode != 4) {
        throw new IllegalArgumentException("Expected ACK packet. Received opcode: " + opcode);
    }

    short blockNumber = buffer.getShort(); // Next 2 bytes are the block number

    return blockNumber & 0xFFFF; // Convert to unsigned integer
}
 }

 */
package bgu.spl.net.impl.tftp.packets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadRequestPacket {

    private static final int DATA_SIZE = 512; // TFTP Data block size
    private static final String FILES_DIRECTORY = "/path/to/your/files/";
    public static FileTransferSession session;

    // Method to initiate the handling of a Read Request (RRQ)
    public static byte[] handleReadAndGetResponse(byte[] message, boolean isLoggedIn) 
    {
        if (!isLoggedIn) {
            return ErrorPacket.createErrorResponse((byte) 6, "User not logged in");
        }
        String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
        Path filePath = Paths.get(FILES_DIRECTORY, filename);

        if (!Files.exists(filePath)) {
            return ErrorPacket.createErrorResponse((byte) 1, "File not found");
        }

        // Assuming this is the first message for the RRQ, prepare the file for reading
        try {
            session = new FileTransferSession(filePath.toString());
            return session.getNextPacket(); // Send the first packet
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ErrorPacket.createErrorResponse((byte) 1, "File not found");
        } catch (IOException e) {
            e.printStackTrace();
            return ErrorPacket.createErrorResponse((byte) 0, "Error reading file");
        }

   
    }

    public static byte[] getNextPacketFromSession() 
    {
        try
        {
            if (session != null) {
                return session.getNextPacket();
            }
        }
     catch (FileNotFoundException e) {
        e.printStackTrace();
        return ErrorPacket.createErrorResponse((byte) 1, "File not found");
    } catch (IOException e) {
        e.printStackTrace();
        return ErrorPacket.createErrorResponse((byte) 0, "Error reading file");
    }
        
        return null;
    }



   

    // Inner class to manage file transfer sessions
    
}
