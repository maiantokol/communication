package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.TftpConnectionsMap;
import bgu.spl.net.impl.tftp.packets.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
                connections.send(connectionId, ReadRequestPacket.handleReadAndGetResponse(message, isLoggedIn, this.state));
                break;
            case 2: // WRQ
                connections.send(connectionId, WriteRequsetPacket.handleWriteAndGetResponse(message, isLoggedIn, state));
                break;


            case 3: // DATA
                if(!state.wrq){
                    return;
                }

                byte[] response = DataPacketHandler.handleDataAndGetResponse(message, isLoggedIn, state);
                connections.send(connectionId, response);
                boolean isAck = getOpCode(response) == (short)4;
                if(isAck && state.shouldReset){
                    for (Integer id : US.getAllIds())
                    {
                        connections.send(id, BcastPacket.createBcastPacket(true, state.wrqFilename));
                    }
                    state.initState();
                }
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

                printBytesInShortFormat(message);
                byte[] responsePacket = LoginRequestPacket.handleLoginAndGetResponse(message, connectionId);

                printBytesInShortFormat(responsePacket);
                if(getOpCode(responsePacket) == (short)4){
                    isLoggedIn = true;
                }
                connections.send(connectionId,responsePacket);
                break;
            case 8: // DELRQ
                byte[] delrqResponse = DeleteRequestPacket.handleDeleteAndGetResponse(message, isLoggedIn);
                connections.send(connectionId, delrqResponse);
                String filename = new String(message, 2, message.length - 3, StandardCharsets.UTF_8);
                boolean isDelrqAck = getOpCode(delrqResponse) == (short)4;
                if(isDelrqAck){
                    for (Integer id : US.getAllIds())
                    {
                        connections.send(id, BcastPacket.createBcastPacket(false, filename));
                    }
                }
                break;


            case 9:
            // These packets end with a zero .
                break;
            case 10: // DISC
                byte[] responsePacketdisc = DiscPacket.createDiscPacket(isLoggedIn);
                if(getOpCode(responsePacketdisc) == (short)4)
                {
                    isLoggedIn = false;
                    US.removeUserById(connectionId);

                }
                connections.send(connectionId,responsePacketdisc);
                break;
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

