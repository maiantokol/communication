package bgu.spl.net.impl.tftp.packets;
import java.util.concurrent.ConcurrentHashMap;

public class UserConnections { 

    private ConcurrentHashMap<String, Integer> userMap = new ConcurrentHashMap<>();

    public void addUser(String username, int id) {
        userMap.put(username, id);
    }

    public Integer getIdByUsername(String username) {
        return userMap.get(username);
    }

    public void removeUser(String username) {
        userMap.remove(username);
    }

    
 }
