package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.TftpConnectionsMap;

public class TftpServer {
    //TODO: Implement this

    public static void main(String[] args) {

        // you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(), //protocol factory
                TftpEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}
