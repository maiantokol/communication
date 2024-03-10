package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.TftpConnectionsMap;
import bgu.spl.net.impl.tftp.packets.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    
    //private ReadRequestPacket currentSession = null;
    int connectionId;
    Connections<byte[]> connections;

    public static Set<String> connectedUsers = Collections.synchronizedSet(new HashSet<>());
    public static Set<Integer> connectedUsersIDS = Collections.synchronizedSet(new HashSet<>());
    public static UserConnections US = new UserConnections();
    


    public boolean isLoggedIn;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) 
    {
        this.connectionId = connectionId;
        this.connections = connections;
        this.isLoggedIn = false;
    }

    @Override
    public void process(byte[] message) 
    {
        // TODO implement this
       boolean firstMessage=true;
        //todo: create "is logged in" boolean, and change it to true after login, and if not logged in, return error packet in each case.
        short opcode = getOpCode(message);
        switch (opcode) {
            case 1: // RRQ
            if (firstMessage) 
               {
                byte[] responsePacket = ReadRequestPacket.handleReadAndGetResponse(message, isLoggedIn);
                if (responsePacket != null) 
                {
                    connections.send(connectionId, responsePacket);
                    // Assuming handleReadAndGetResponse initiates a session and stores it as a class member
                    firstMessage = false;
                }
            }

            //TODO: return an error

            case 2: // WRQ
            connections.send(connectionId, WriteRequsetPacket.handleWriteAndGetResponse(message, isLoggedIn));


            case 3: // DATA

            connections.send(connectionId, DataPacketHandler.handleDataAndGetResponse(message, isLoggedIn,connections,connectedUsersIDS));

            case 4: // ACK packet
                // ACK packet  2 bytes for the opcode + 2 bytes for the block number
                if (ReadRequestPacket.session != null)
                {
                    byte[] nextDataPacket = ReadRequestPacket.getNextPacketFromSession();
                    if (nextDataPacket != null) {
                        connections.send(connectionId, nextDataPacket);
                    } else 
                    {
                        // If null, assuming the transfer is complete, you may want to clean up
                        ReadRequestPacket.session = null; // Reset for the next transfer
                    }
            }
            //TODO: ELSE???
            break;
                
             
            case 5: // ERROR packet
            case 6: // DIRQ
                    List<byte[]> packetlist = DirqPacket.createDirqResponse(isLoggedIn);
                    for (byte[] bs : packetlist) 
                    {
                        connections.send(connectionId, bs); //it need to wait for an approve??
                    }

            case 7: // LOGRQ
                byte[] responsePacket = LoginRequestPacket.handleLoginAndGetResponse(message);
                if(getOpCode(responsePacket) == (short)4){
                    isLoggedIn = true;
                    connectedUsersIDS.add(connectionId);
                }
                connections.send(connectionId,responsePacket);
            case 8: // DELRQ
                connections.send(connectionId, DeleteRequestPacket.handleDeleteAndGetResponse(message, isLoggedIn,connections,connectedUsersIDS));


            case 9:
            // These packets end with a zero .
            case 10: // DISC
            byte[] responsePacketdisc = DiscPacket.createDiscPacket(isLoggedIn);
            if(getOpCode(responsePacketdisc) == (short)4){
                isLoggedIn = false;
                connectedUsersIDS.remove(connectionId);
                connectedUsers.remove("string username"); //TODO: find the string username
            }
            connections.send(connectionId,responsePacketdisc); 
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    }

    private short getOpCode(byte[] message)
    {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 0,2)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public boolean isLoggedIn ()
    {
        return isLoggedIn;
    }
}

