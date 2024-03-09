package bgu.spl.net.srv;
import java.util.concurrent.ConcurrentHashMap;
public class TftpConnectionsMap implements Connections<byte[]> {

    private ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> connectionsMap = new ConcurrentHashMap<>();

    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        connectionsMap.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
        ConnectionHandler<byte[]> handler = connectionsMap.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void disconnect(int connectionId) {
        connectionsMap.remove(connectionId);
    }

    public int getNumberOfConnections() {
        return connectionsMap.size();
    }
}

