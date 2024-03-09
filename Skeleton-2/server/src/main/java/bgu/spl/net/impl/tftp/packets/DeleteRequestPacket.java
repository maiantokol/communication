package bgu.spl.net.impl.tftp.packets;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class DeleteRequestPacket {
    private static final String Flies_Folder_Path = "../../../../../../../../../Flies/";
    public static byte[] handleDeleteAndGetResponse(byte[] message, boolean isLoggedIn){
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
        return AckPacket.getAckPacket((byte)0);;

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
