package bgu.spl.net.impl.tftp;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

public class TftpClientProtocol {

    private static final int BLOCK_SIZE = 512;
    private static final String FILES_DIRECTORY = "."+File.separator;

   // private boolean isLoggedIn;
    //public  StateClient state;
    //private TftpClientEncoderDecoder encoderDecoder; // Assuming you have this class implemented
   // private TftpClient client; // This should be your client class that can send and receive packets

    public TftpClientProtocol() {
       // this.isLoggedIn = false;
       // this.state = new StateClient();
     //   this.encoderDecoder = encoderDecoder;

      //  this.client = client;
    }

    public byte[] process(byte[] message) {
        short opcode = getOpCode(message);

        switch (opcode) {
            case 3: // data packet
                // Handle DATA packet, save data and send ACK
                if (StateClient.dirq)
                {

                    short dataSize = getDataSize(message);
                    //System.out.println("[handleDataAndGetResponse] dataSize is: "+ dataSize);
                    byte[] data = Arrays.copyOfRange(message, 6, 6 + dataSize);
                    StateClient.dataBlocks.add(data);
                    short blockNumber = getBlockNumber(message);
                    //System.out.println("[handleDataAndGetResponse] block number is: "+blockNumber);

                    if(dataSize < 512){
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
                        printDirqListing(combinedArray);
                        StateClient.initState();
                    }

                    return AckPacketClient.getAckPacket(blockNumber);
                }

                if(StateClient.rrq){
                    short dataSize = getDataSize(message);

                    byte[] data = Arrays.copyOfRange(message, 6, 6 + dataSize);
                    StateClient.dataBlocks.add(data);
                    short blockNumber = getBlockNumber(message);

                    if(dataSize < 512){
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

                        try (FileOutputStream fos = new FileOutputStream(StateClient.fileName)) {
                            fos.write(combinedArray);
                        } catch (IOException e) {
                            System.out.println("[handleDataAndGetResponse] error "+e.toString());

                        }
                        System.out.println("RRQ " + StateClient.fileName +" complete");
                        StateClient.initState();
                    }

                    return AckPacketClient.getAckPacket(blockNumber);

                }

                else
                {
                    if(!StateClient.rrq && StateClient.fileName!=null) {
                        //String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                        
//                        Path filePath = Paths.get(FILES_DIRECTORY, StateClient.fileName);
//                        if (Files.exists(filePath))
//                        {
//                            return ErrorPacket((byte) 1, "File already exists");
//                        }


                        return AckPacketClient.getAckPacket((short)0);
    
                    }

                }
                

        break;
        
            
            case 4: // ACK packet
                // Print ACK message
                short blockNumber= ByteBuffer.wrap(message,2,2).getShort();
                System.out.println("ACK "+blockNumber);

//                    if(StateClient.wrq && StateClient.fileName!=null)
//                    {
//                        Path filePath = Paths.get(FILES_DIRECTORY,  StateClient.fileName);
//                        if (!Files.exists(filePath)) {
//                            return ErrorPacket((byte) 1, "File not found");
//                        }
//                        try {
//                            List<byte[]> fileBlocks = WritePacket.writeFileInBlocks(filePath.toString());
//                            StateClient.wrq = true;
//                            StateClient.numOfBlocks = fileBlocks.size();
//                            StateClient.dataBlocks = fileBlocks;
//                            StateClient.blocksSent = 1;
//                            // send first data block
//
//                          //  return WritePacket.createDataPacket(StateClient.blocksSent, StateClient.dataBlocks.get(0), StateClient.dataBlocks.get(0).length);
//
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            return ErrorPacket((byte) 1, "File not found");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            return ErrorPacket((byte) 0, "Error reading file");
//                        }
//                    }



                    if (StateClient.wrq)
                    {
                        if (StateClient.blocksSent == StateClient.numOfBlocks){
                            System.out.println("WRQ " + StateClient.fileName +" complete");
                            StateClient.initState();
                            return null;
                        }
                        StateClient.blocksSent++;
                        byte[] dataBlock = StateClient.dataBlocks.get(StateClient.blocksSent - 1);
                        byte[] dataPacket = WritePacket.createDataPacket(StateClient.blocksSent, dataBlock, dataBlock.length);
                        return dataPacket;
                    }
        
                break;

            case 5: // ERROR packet
                // Handle error, print error message
                handleErrorPacket(message);
                break;

            case 9: // BCAST packet
                // Print BCAST message
                handleBcastPacket(message);
                break;

            // Add more cases if needed

            default:
                System.out.println("Received unknown opcode: " + opcode);
                break;
        }
        return null ; 
    }

    

    private void handleRRQPacket(String filename) {
        // Extract data and save to file or buffer based on your application's need
        System.out.println("RRQ " + filename +" complete");
        // Send ACK for DATA packet here...
    }

    private void handleDataPacket(byte[] message) {
        // Extract data and save to file or buffer based on your application's need
        System.out.println("Received DATA packet");
        // Send ACK for DATA packet here...
    }

    private void handleErrorPacket(byte[] message) {
        // Print error message
        String errorMessage = new String(message, 4, message.length - 5);
        System.out.println("Error: " + errorMessage);
    }

    private void handleBcastPacket(byte[] message) {
        // Print BCAST message
        byte addOrDel = message[2];
        String status = (addOrDel == 1) ? "add" : "del";
        String filename = new String(message, 3, message.length - 4);
        System.out.println("BCAST " + status + " " + filename);
    }

    private short getOpCode(byte[] message) {
        return ByteBuffer.wrap(message).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public static byte[] ErrorPacket(byte errorCode, String errorMessage) {
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


    public static String getDirectoryListing(String path) {
        File directory = new File(path);
        StringBuilder listing = new StringBuilder();

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    listing.append(file.getName()).append((char) 0);
                }
            } else {
                return "The specified path is not a directory or an error occurred.";
            }
        } else {
            return "The specified path is not a directory.";
        }
        return listing.toString();
    }

    public static List<byte[]> sliceByteArray(byte[] byteArray, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        int start = 0;
        while (start < byteArray.length) {
            int end = Math.min(byteArray.length, start + chunkSize);
            byte[] chunk = new byte[end - start];
            System.arraycopy(byteArray, start, chunk, 0, chunk.length);
            chunks.add(chunk);
            start += chunkSize;
        }
        return chunks;
    }

    private static short getDataSize(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 2,4)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    private static short getBlockNumber(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 4,6)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    private static void printDirqListing(byte[] message)
    {
        StringBuilder currentString = new StringBuilder();
        for (byte b : message) {
            if (b == 0) {
                if(currentString.length() > 0) {
                    System.out.println(currentString.toString());
                    currentString.setLength(0);
                }
            } else {
                currentString.append((char) b);
            }
        }
        // Check if there is any remaining string to print after the last zero byte
        if (currentString.length() > 0) {
            System.out.println(currentString.toString());
        }
    }

}
