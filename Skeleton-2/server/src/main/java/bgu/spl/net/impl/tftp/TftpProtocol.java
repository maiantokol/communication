package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.TftpConnectionsMap;
import bgu.spl.net.impl.tftp.packets.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static bgu.spl.net.impl.tftp.TftpEncoderDecoder.printBytesInShortFormat;
import static bgu.spl.net.impl.tftp.packets.ReadRequestPacket.createDataPacket;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    
    //private ReadRequestPacket currentSession = null;
    int connectionId;
    Connections<byte[]> connections;

    public static Set<String> connectedUsers = Collections.synchronizedSet(new HashSet<>());
    public static Set<Integer> connectedUsersIDS = Collections.synchronizedSet(new HashSet<>());
    public static UserConnections US = new UserConnections();
    public State state;

    public boolean isLoggedIn;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) 
    {
        System.out.println("[start] connectionId: "+connectionId);
        System.out.println("[start] connections: "+connections.toString());
        this.connectionId = connectionId;
        this.connections = connections;
        this.isLoggedIn = false;
        this.state = new State();

    }

    @Override
    public void process(byte[] message) 
    {
        // TODO implement this
        short opcode = getOpCode(message);
        switch (opcode) {
            case 1: // RRQ
                System.out.println("in RRQ");
                printBytesInShortFormat(message);
                connections.send(connectionId, ReadRequestPacket.handleReadAndGetResponse(message, isLoggedIn, this.state));
                break;
            case 2: // WRQ
                connections.send(connectionId, WriteRequsetPacket.handleWriteAndGetResponse(message, isLoggedIn, state));
                break;


            case 3: // DATA
                if(!state.wrq){
                    return;
                }
                connections.send(connectionId, DataPacketHandler.handleDataAndGetResponse(message, isLoggedIn,state));
                break;

            case 4: // ACK packet
                if(state.rrq || state.dirq){
                    if (state.blocksSent == state.numOfBlocks){
                        // finished rrq
                        state.initState();
                        return;
                    }

                    state.blocksSent++;
                    byte[] dataBlock = state.dataBlocks.get(state.blocksSent - 1);
                    byte[] dataPacket =  createDataPacket(state.blocksSent, dataBlock , dataBlock.length);
                    connections.send(connectionId, dataPacket);
                }

                break;

                // ACK packet  2 bytes for the opcode + 2 bytes for the block number
//                if (ReadRequestPacket.session != null)
//                {
//                try {
//                    byte[] nextDataPacket = ReadRequestPacket.getNextPacketFromSession();
//                    if (nextDataPacket != null) {
//                        connections.send(connectionId, nextDataPacket);
//                    } else
//                    {
//                        // If null, assuming the transfer is complete, you may want to clean up
//                        ReadRequestPacket.session = null; // Reset for the next transfer
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    // Handle error, maybe send an error packet back???????????
//                    }
//                }
            case 5: // ERROR packet
            case 6: // DIRQ
                connections.send(connectionId, DirqPacket.createDirqResponse(isLoggedIn, state));
                break;

            case 7: // LOGRQ
                System.out.println("in logrq, got message");
                printBytesInShortFormat(message);
                byte[] responsePacket = LoginRequestPacket.handleLoginAndGetResponse(message);
                System.out.println("in logrq, response packet is");
                printBytesInShortFormat(responsePacket);
                if(getOpCode(responsePacket) == (short)4){
                    System.out.println("in logrq, getOpCode(responsePacket) == (short)4 true");
                    isLoggedIn = true;
                    connectedUsersIDS.add(connectionId);
                }
                System.out.println("in logrq, before conenction send");
                connections.send(connectionId,responsePacket);
                break;
            case 8: // DELRQ
                System.out.println("[DELRQ]");
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
        //throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
        return false;
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

