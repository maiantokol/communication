package bgu.spl.net.impl.tftp.packets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTransferSession
{
    private static final int DATA_SIZE = 512; // TFTP Data block size

    private FileInputStream fis;
    private int blockNumber = 1; // Start with block number 1

    public FileTransferSession(String filePath) throws FileNotFoundException {
        this.fis = new FileInputStream(filePath);
    }

    public byte[] getNextPacket() throws IOException {
        if (fis == null) {
            return null; // End of the session
        }

        byte[] dataBlock = new byte[DATA_SIZE];
        int bytesRead = fis.read(dataBlock);

        if (bytesRead == -1) { // End of file
            fis.close();
            fis = null; // Mark the end of the session
            return null;
        }

        // Adjust the last data block if it's smaller than DATA_SIZE
        if (bytesRead < DATA_SIZE)
        {
            dataBlock = ByteBuffer.wrap(dataBlock, 0, bytesRead).array();
        }

        return createDataPacket(blockNumber++, dataBlock, bytesRead);
    }

    private byte[] createDataPacket(int blockNumber, byte[] data, int bytesRead) {
        ByteBuffer dataPacket = ByteBuffer.allocate(4 + 2 + bytesRead); // Opcode (2) + Block # (2) + Data Size (2) + Data
        dataPacket.putShort((short) 3); // Opcode for DATA
        dataPacket.putShort((short) bytesRead); // Include data size after opcode as per the new requirement
        dataPacket.putShort((short) blockNumber); // Block number
        dataPacket.put(data, 0, bytesRead); // Actual file data
        return dataPacket.array();
    }
}