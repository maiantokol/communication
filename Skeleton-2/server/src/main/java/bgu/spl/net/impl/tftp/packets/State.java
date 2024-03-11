package bgu.spl.net.impl.tftp.packets;

import java.util.List;

public class State {
    public boolean dirq = false;
    public boolean rrq = false;
    public int blocksSent = 0;
    public String filename = "";
    public int numOfBlocks = 0;
    public List<byte[]> dataBlocks;

    public void initState(){
        dirq = false;
        rrq = false;
        blocksSent = 0;
        filename = "";
        numOfBlocks = 0;
        dataBlocks = null;
    }

}
