package bgu.spl.net.impl.tftp.packets;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class UserConnections {

    private ConcurrentHashMap<String, Integer> userMap = new ConcurrentHashMap<>();

    public void addUser(String username, int id) {
        userMap.put(username, id);
    }

    public Integer getIdByUsername(String username) {
        return userMap.get(username);
    }
    public boolean contains(String username) {
        return userMap.get(username)!=null;
    }

    public void removeUser(String username) {
        userMap.remove(username);
    }

    public Integer[] getAllIds() {
        Collection<Integer> ids = userMap.values();
        return ids.toArray(new Integer[0]);
    }
    public String getUsernameById(int id) {
        for (Entry<String, Integer> entry : userMap.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null; // or throw an exception if you prefer
    }
    public void removeUserById(int id) {
        String username = getUsernameById(id);
        if (username != null) {
            removeUser(username);
        }
    }


}