package bgu.spl.net.impl.tftp;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TtfpConnections<T> implements Connections<T> {
    
    private ConcurrentHashMap<int, ConcurrentLinkedQueue<String>> channels = new ConcurrentHashMap<>();


     void connect(int connectionId, ConnectionHandler<T> handler)
     {
        throw new UnsupportedOperationException("Unimplemented method 'start'");

     }

    boolean send(int connectionId, T msg)
    {
        throw new UnsupportedOperationException("Unimplemented method 'start'");

    }

    void disconnect(int connectionId)
    {
        throw new UnsupportedOperationException("Unimplemented method 'start'");

    }
}
