package bgu.spl.net.impl.tftp.packets;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import bgu.spl.net.srv.Connections;

public class DeleteRequestPacket {
    private static final String Flies_Folder_Path = "/Users/maiantokol/Library/Mobile Documents/com~apple~CloudDocs/Downloads/לימודים/שנה ב/spl/project3/Skeleton-2/server/Flies";
    public static byte[] handleDeleteAndGetResponse(byte[] message, boolean isLoggedIn,Connections<byte[]> connections , Set<Integer> connectedUsersIDS){
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }
        String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
        if(!fileExists(filename)){
            return ErrorPacket.createErrorResponse((byte)1,"File Not Found");
        }
        if(!deleteFile(filename)){
            return ErrorPacket.createErrorResponse((byte)1,"File Not Found");
        }
        for (Integer id : connectedUsersIDS) 
        {
            connections.send(id, BcastPacket.createBcastPacket(true, filename));
        }
        return AckPacket.getAckPacket((byte)0);

    }

    public static boolean fileExists(String filename) {
        File file = new File(Flies_Folder_Path + filename);
        return file.exists();
    }

    public static boolean deleteFile(String filename) {
        File file = new File(Flies_Folder_Path + filename);
        return file.exists() && file.delete();
    }
}
