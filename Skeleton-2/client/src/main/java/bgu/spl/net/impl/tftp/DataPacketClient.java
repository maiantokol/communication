package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.StateClient;

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

import static bgu.spl.net.impl.tftp.TftpClientProtocol.ErrorPacket;


public class DataPacketClient {
    private static final int DATA_SIZE = 512;

    public static byte [] DataPacket(byte[] message) {

        short dataSize = getDataSize(message);
        //System.out.println("[handleDataAndGetResponse] dataSize is: "+ dataSize);
        byte[] data = Arrays.copyOfRange(message, 6, 6 + dataSize);
        StateClient.dataBlocks.add(data);
        short blockNumber = getBlockNumber(message);
        //System.out.println("[handleDataAndGetResponse] block number is: "+blockNumber);

        if(dataSize < DATA_SIZE){
            int totalSize = 0;
            for (byte[] byteArray : StateClient.dataBlocks) {
                totalSize += byteArray.length;
            }

            // Create the combined array and copy each byte array into it
            byte[] combinedArray = new byte[totalSize];
            int currentPosition = 0;
            for (byte[] byteArray : StateClient.dataBlocks) {
                System.arraycopy(byteArray, 0, combinedArray, currentPosition, byteArray.length);
                currentPosition += byteArray.length;
            }

            try (FileOutputStream fos = new FileOutputStream(StateClient.rrqFilepath)) {
                fos.write(combinedArray);
            } catch (IOException e) {
                System.out.println("[handleDataAndGetResponse] error "+e.toString());
                return ErrorPacket((byte) 0, "Could not write file");
            }
            StateClient.shouldReset = true;
        }

        return AckPacketClient.getAckPacket(blockNumber);
    }


    private static short getDataSize(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 2,4)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    private static short getBlockNumber(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 4,6)).order(ByteOrder.BIG_ENDIAN).getShort();
    }






}