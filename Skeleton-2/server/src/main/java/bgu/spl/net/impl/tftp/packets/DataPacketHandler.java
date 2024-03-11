package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.srv.Connections;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;


public class DataPacketHandler {
    private static final int DATA_SIZE = 512;
    private static final String FILES_DIRECTORY = "C:\\Users\\user\\Desktop\\communication\\Skeleton-2\\server\\Flies";
    Connections<byte[]> connections;

   static ArrayList<byte[]> arrayOfDataPacket = new ArrayList<byte[]>();

    public static byte [] handleDataAndGetResponse(byte[] message, boolean isLoggedIn, Connections<byte[]> connections , Set<Integer> connectedUsersIDS)
     {

    int messageSize = message.length-6;
    boolean transferComplete =  messageSize < DATA_SIZE;
    startFileTransfer(message);

    if(transferComplete)
        {
         byte[] CompletePacket= buldingPacket(arrayOfDataPacket); 
         //to return the final block number ens that we completed

         String filename = WriteRequsetPacket.getName();
    Path filePath = Paths.get(FILES_DIRECTORY +"\\"+ filename);
//
    try 
    {   
    Files.write(filePath, CompletePacket); // Write the bytes to the file
    }   
    catch (IOException e) {
        e.printStackTrace();
    // Handle the exception here???????
        }
    for (Integer id : TftpProtocol.US.getAllIds())
    {
        
        connections.send(id, BcastPacket.createBcastPacket(true, filename));
    }

        }
 
        short blocknumber = getBlockNumber(message);
        return AckPacket.getAckPacket(blocknumber); // send ack for block 0 

    }

private static void startFileTransfer(byte[] message)  
{

    byte[] dataPacket = createDataPacket(message);
     arrayOfDataPacket.add(dataPacket);


}

private static byte[] createDataPacket (byte[] message)
{
    byte[] newArray = Arrays.copyOfRange(message, 6, message.length);
    return newArray;
}

private static byte[] buldingPacket(ArrayList<byte[]> arr)
    {   
// Determine the total size of the new array
    int totalSize = arrayOfDataPacket.stream().mapToInt(a -> a.length).sum();
    ByteBuffer buffer = ByteBuffer.allocate(totalSize);

// Concatenate all byte arrays
for (byte[] packet : arrayOfDataPacket) 
{
    buffer.put(packet);
}

return buffer.array();    
}  

 private static short getBlockNumber(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 5,7)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

  


}