package bgu.spl.net.impl.tftp;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TftpClientProtocol {

    private static final int BLOCK_SIZE = 512;
    private static final String FILES_DIRECTORY = "C:\\Users\\idozA\\Desktop\\spl3\\communication\\Skeleton-2\\server\\Flies";

    private boolean isLoggedIn;
    public StateClient state;
    private TftpClientEncoderDecoder encoderDecoder; // Assuming you have this class implemented
    private TftpClient client; // This should be your client class that can send and receive packets

    public TftpClientProtocol() {
        this.isLoggedIn = false;
        this.state = new StateClient();
     //   this.encoderDecoder = encoderDecoder;
      //  this.client = client;
    }

    public byte[] process(byte[] message) {
        short opcode = getOpCode(message);

        switch (opcode) {
            case 1: // DATA packet
                // Handle DATA packet, save data and send ACK
                if(!state.rrq && state.fileName!=null) {
                    String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                    Path filePath = Paths.get(FILES_DIRECTORY, filename);
                    if (Files.exists(filePath)) {
                        return ErrorPacket((byte) 1, "File already exists");
                    }
                    state.rrq = true;
                    state.dataBlocks = new ArrayList<>();
                    state.rrqFilename = filename;
                    state.rrqFilepath = String.valueOf(filePath);
                }
                else if (state.rrq){
                   return DataPacketClient.DataPacket(message,state) ;
            }

        break;
            case 3: // DATA packet
                // Handle DATA packet, save data and send ACK
                handleDataPacket(message);
                break;

            case 4: // ACK packet
                // Print ACK message
                short blockNumber= ByteBuffer.wrap(message,2,2).getShort();
                System.out.println("ACK "+blockNumber);
                if(state.wrq || state.dirq){ //change
                    if (state.blocksSent == state.numOfBlocks){
                        // finished wrq //change raz
                        state.initState();
                        return null;
                    }
                    if(!state.wrq && state.fileName!=null){
                        Path filePath = Paths.get(FILES_DIRECTORY,  state.fileName);
                        if (!Files.exists(filePath)) {
                            return ErrorPacket((byte) 1, "File not found");
                        }
                        try {
                            List<byte[]> fileBlocks = WritePacket.writeFileInBlocks(filePath.toString());
                            state.wrq = true;
                            state.numOfBlocks = fileBlocks.size();
                            state.dataBlocks = fileBlocks;
                            state.blocksSent = 1;
                            // send first data block

                            return WritePacket.createDataPacket(state.blocksSent, state.dataBlocks.get(0), state.dataBlocks.get(0).length);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return ErrorPacket((byte) 1, "File not found");
                        } catch (IOException e) {
                            e.printStackTrace();
                            return ErrorPacket((byte) 0, "Error reading file");
                        }

                    }
                    else if (state.wrq) {
                        state.blocksSent++;
                        byte[] dataBlock = state.dataBlocks.get(state.blocksSent - 1);
                        byte[] dataPacket = WritePacket.createDataPacket(state.blocksSent, dataBlock, dataBlock.length);
                        return dataPacket;
                    }
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
    }

    private void handleRRQPacket(byte[] message) {
        // Extract data and save to file or buffer based on your application's need
        String filename = new String(message, 3, message.length - 4);
        System.out.println("RRQ " + filename +"complete");
        // Send ACK for DATA packet here...
    }

    private void handleDataPacket(byte[] message) {
        // Extract data and save to file or buffer based on your application's need
        System.out.println("Received DATA packet");
        // Send ACK for DATA packet here...
    }

    private void handleAckPacket(byte[] message) {
        // Extract block number and print ACK message
        ByteBuffer wrap = ByteBuffer.wrap(message).order(ByteOrder.BIG_ENDIAN);
        short blockNumber = wrap.getShort(2);
        System.out.println("ACK " + blockNumber);
    }

    private void handleErrorPacket(byte[] message) {
        // Print error message
        String errorMessage = new String(message, 4, message.length - 5);
        System.out.println("Error: " + errorMessage);
    }

    private void handleBcastPacket(byte[] message) {
        // Print BCAST message
        byte addOrDel = message[2];
        String status = (addOrDel == 1) ? "added" : "deleted";
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
}
