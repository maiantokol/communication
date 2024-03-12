package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.packets.ErrorPacket;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static bgu.spl.net.impl.tftp.packets.ReadRequestPacket.createDataPacket;

public class DirqPacket {

    private static final short OPCODE = 6;
    private static final String FILES_DIRECTORY =  "C:\\Users\\idozA\\Desktop\\spl3\\communication\\Skeleton-2\\server\\Flies";

    public static byte[] createDirqResponse(boolean isLoggedIn, State state) {
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }
        String directoryListing = getDirectoryListing(FILES_DIRECTORY);
        byte[] directoryListingBytes = directoryListing.getBytes();
        List<byte[]> chunks = sliceByteArray(directoryListingBytes, 512);

        state.dirq = true;

        state.numOfBlocks = chunks.size();
        state.dataBlocks = chunks;
        state.blocksSent = 1;

        System.out.println("[handleReadAndGetResponse] print file blocks:");
        for(byte[] block : chunks){
            TftpEncoderDecoder.printBytesInShortFormat(block);
            System.out.println();
        }

        // send first data block

        return createDataPacket(state.blocksSent, state.dataBlocks.get(0), state.dataBlocks.get(0).length);
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
}
