package bgu.spl.net.impl.tftp;

import java.util.List;

public class StateClient {
    public static boolean dirq = false;
    public static boolean rrq = false;
    public static boolean wrq = false;
    public static int blocksSent = 0;
    public static int numOfBlocks = 0;
    public static int sentWrqBlocks = 0;
    public static String rrqFilename = "";
    public static String rrqFilepath = "";
    public static String wrqFilepath = "";
    public static boolean shouldReset = false;

    public static String fileName;
    public static List<byte[]> dataBlocks;


    public static void initState(){
        dirq = false;
        rrq = false;
        wrq = false;
        blocksSent = 0;
        rrqFilename = "";
        rrqFilepath = "";
        wrqFilepath = "";
        numOfBlocks = 0;
        sentWrqBlocks = 0;
        shouldReset = false;
        dataBlocks = null;
        fileName=null;
    }

}
