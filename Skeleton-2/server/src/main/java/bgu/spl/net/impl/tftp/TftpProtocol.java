package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    int connectionId;
    Connections<byte[]> connections;

    public static Set<String> connectedUsers = Collections.synchronizedSet(new HashSet<>());

    public boolean isLoggedIn;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        // TODO implement this
        this.connectionId = connectionId;
        this.connections = connections;
        this.isLoggedIn = false;



    }

    @Override
    public void process(byte[] message) {
        // TODO implement this

        //todo: create "is logged in" boolean, and change it to true after login, and if not logged in, return error packet in each case.
        short opcode = getOpCode(message);
        switch (opcode) {
            case 1: // RRQ
            case 2: // WRQ
            case 3: // DATA
                if (len < 4) return false;
                short packetSize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 2, 4)).order(ByteOrder.BIG_ENDIAN).getShort();
                // DATA packet length = 2 bytes opcode + 2 bytes packet size + 2 bytes block number + packet size
                return len == 4 + 2 + packetSize;
            case 4: // ACK packet
                // ACK packet  2 bytes for the opcode + 2 bytes for the block number
                return len == 4;
            case 5: // ERROR packet
                // ERROR packets have a 4-byte header (opcode + error code) followed by an error message terminated with zero
                return len > 4 && bytes[len - 1] == 0;
            case 6: // DIRQ
            case 7: // LOGRQ
                byte[] responsePacket = LoginRequestPacket.handleLoginAndGetResponse(message);
                if(getOpCode(responsePacket) == (short)4){
                    isLoggedIn = true;
                }
                connections.send(connectionId,responsePacket);
            case 8: // DELRQ
                connections.send(connectionId, DeleteRequestPacket.handleDeleteAndGetResponse(message, isLoggedIn));

            case 9: // BCAST
                return len>3 & bytes[len-1]==0;
            // These packets end with a zero .
            case 10: // DISC
                // These packets are  2 bytes .
                return len == 2;



            default:
                return false;
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    }

    private short getOpCode(byte[] message){
        return ByteBuffer.wrap(Arrays.copyOfRange(message, 0,2)).order(ByteOrder.BIG_ENDIAN).getShort();
    }


    
}
