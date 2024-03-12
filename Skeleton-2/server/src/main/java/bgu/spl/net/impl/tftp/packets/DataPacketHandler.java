package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.srv.Connections;

import java.io.File;
import java.io.FileOutputStream;
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

    public static byte [] handleDataAndGetResponse(byte[] message, boolean isLoggedIn, State state, Connections<byte[]> connections, Set<Integer> connectedUsersIDS) {
        if (!isLoggedIn) {
            return ErrorPacket.createErrorResponse((byte) 6, "User not logged in");
        }

        short dataSize = getDataSize(message);
        System.out.println("[handleDataAndGetResponse] dataSize is: "+ dataSize);
        byte[] data = Arrays.copyOfRange(message, 6, 6 + dataSize);
        System.out.println("[handleDataAndGetResponse] this is the data array");
        TftpEncoderDecoder.printBytesInShortFormat(data);;
        state.dataBlocks.add(data);
        short blockNumber = getBlockNumber(message);
        System.out.println("[handleDataAndGetResponse] block number is: "+blockNumber);

        if(dataSize < DATA_SIZE){
            int totalSize2 = 0;
            for (byte[] dataBlock : state.dataBlocks) {
                totalSize2 += dataBlock.length;
                System.out.println("[handleDataAndGetResponse] this is the dataBlock array");
                TftpEncoderDecoder.printBytesInShortFormat(dataBlock);;

            }
            System.out.println("[handleDataAndGetResponse] total size 2 is"+totalSize2);

            int totalSize = 0;
            for (byte[] byteArray : state.dataBlocks) {
                totalSize += byteArray.length;
            }

            // Create the combined array and copy each byte array into it
            byte[] combinedArray = new byte[totalSize];
            int currentPosition = 0;
            for (byte[] byteArray : state.dataBlocks) {
                System.arraycopy(byteArray, 0, combinedArray, currentPosition, byteArray.length);
                currentPosition += byteArray.length;
            }
            System.out.println("[handleDataAndGetResponse] this is the combined array");
            TftpEncoderDecoder.printBytesInShortFormat(combinedArray);;

            System.out.println("[handleDataAndGetResponse] state.wrqFilepath is  "+state.wrqFilepath);
            try (FileOutputStream fos = new FileOutputStream(state.wrqFilepath)) {
                fos.write(combinedArray);
            } catch (IOException e) {
                System.out.println("[handleDataAndGetResponse] error "+e.toString());
                return ErrorPacket.createErrorResponse((byte) 0, "Could not write file");
            }
            state.shouldReset = true;
        }

        return AckPacket.getAckPacket(blockNumber);
    }

//private static void startFileTransfer(byte[] message)
//{
//
//    byte[] dataPacket = createDataPacket(message);
//     arrayOfDataPacket.add(dataPacket);
//
//
//}
//
//private static byte[] createDataPacket (byte[] message)
//{
//    byte[] newArray = Arrays.copyOfRange(message, 6, message.length);
//    return newArray;
//}
//
//private static byte[] buldingPacket(ArrayList<byte[]> arr)
//    {
//// Determine the total size of the new array
//    int totalSize = arrayOfDataPacket.stream().mapToInt(a -> a.length).sum();
//    ByteBuffer buffer = ByteBuffer.allocate(totalSize);
//
//// Concatenate all byte arrays
//for (byte[] packet : arrayOfDataPacket)
//{
//    buffer.put(packet);
//}
//
//return buffer.array();
//}
    private static short getDataSize(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 2,4)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

     private static short getBlockNumber(byte[] message)
        {
            return ByteBuffer.wrap(Arrays.copyOfRange(message, 4,6)).order(ByteOrder.BIG_ENDIAN).getShort();
        }



  


}