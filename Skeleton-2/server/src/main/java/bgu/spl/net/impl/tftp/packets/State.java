package bgu.spl.net.impl.tftp.packets;

import java.util.List;

public class State {
    public boolean dirq = false;
    public boolean rrq = false;
    public boolean wrq = false;
    public int blocksSent = 0;
    public int numOfBlocks = 0;
    public int sentWrqBlocks = 0;
    public String wrqFilepath = "";
    public List<byte[]> dataBlocks;

    public void initState(){
        dirq = false;
        rrq = false;
        wrq = false;
        blocksSent = 0;
        wrqFilepath = "";
        numOfBlocks = 0;
        sentWrqBlocks = 0;
        dataBlocks = null;
    }

}
