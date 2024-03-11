package bgu.spl.net.impl.tftp.packets;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.srv.Connections;


public class DeleteRequestPacket {
    private static final String Flies_Folder_Path = "C:\\Users\\user\\Desktop\\communication\\Skeleton-2\\server\\Flies";

    public static byte[] handleDeleteAndGetResponse(byte[] message, boolean isLoggedIn,Connections<byte[]> connections , Set<Integer> connectedUsersIDS){
      //  System.out.println("[handleDeleteAndGetResponse] start, Flies_Folder_Path is "+ Flies_Folder_Path);
        if(!isLoggedIn){
            return ErrorPacket.createErrorResponse((byte)6,"User not logged in");
        }
        String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);

        //System.out.println("[handleDeleteAndGetResponse] filename: "+filename);
        if(!fileExists(filename)){
            return ErrorPacket.createErrorResponse((byte)1,"File Not Found");
        }
       // System.out.println("[handleDeleteAndGetResponse] file exist. ");


        if(!deleteFile(filename))
        {
            //System.out.println("why im here?  "+!deleteFile(filename));

            return ErrorPacket.createErrorResponse((byte)1,"File Not Found ");
        }
        //System.out.println("[handleDeleteAndGetResponse] file deleted");


        for (Integer id : TftpProtocol.US.getAllIds())
        {
            connections.send(id, BcastPacket.createBcastPacket(false, filename));
        }
        System.out.println("ghhfhfh");
        return AckPacket.getAckPacket((byte)0);

    }

    public static boolean fileExists(String filename) {
        File file = new File(Flies_Folder_Path +"\\"+ filename);
        //System.out.println("Looking for file at: " + file.getAbsolutePath());
        return file.exists();
    }

    public static boolean deleteFile(String filename) {
        File file = new File(Flies_Folder_Path +"\\"+ filename);
       // System.out.println("file.exists: "+file.exists()+" "+"file.delete "+file.delete());
        return file.delete();
    }
}
