package bgu.spl.net.impl.tftp.packets;

import java.nio.charset.StandardCharsets;

import bgu.spl.net.srv.Connections;

import java.nio.ByteBuffer;

public class BcastPacket {

    // Opcode for BCAST packets
    private static final short OPCODE = 9;

    // Flags for file added or deleted
    private static final byte FILE_ADDED = 1;
    private static final byte FILE_DELETED = 0;
    //Connections<byte[]> connections;


    /**
     * Creates a BCAST packet byte array to notify clients about file addition or deletion.
     *
     * @param isAdded  true if the file is added, false if deleted.
     * @param filename the name of the file that was added or deleted.
     * @return the BCAST packet as a byte array.
     */
    public static byte[] createBcastPacket(boolean isAdded, String filename)
     {
        
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(3 + filenameBytes.length + 1);
        buffer.putShort(OPCODE); // Opcode for BCAST
        buffer.put(isAdded ? FILE_ADDED : FILE_DELETED); // Added or deleted flag
        buffer.put(filenameBytes); // Filename
        buffer.put((byte) 0); // Zero byte to terminate the string
        return buffer.array();

    }
    
}
