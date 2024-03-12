package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.ListeningThread;
//import bgu.spl.net.impl.tftp.KeyboardThread;

import java.io.*;
import java.net.Socket;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"localhost"};
        }
        try(Socket socket = new Socket(args[0], 7777);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            TftpClientEncoderDecoder encdec = new TftpClientEncoderDecoder();
            TftpClientProtocol protocol = new TftpClientProtocol();

            ListeningThread listeningThread = new ListeningThread(in, out, protocol, encdec);


            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            listeningThread.start();
            try {
                String userInput;
                while (!socket.isClosed() && (userInput = keyboard.readLine()) != null) {
                    if (userInput != null) {
                        out.write(encdec.encode(userInput.getBytes()));
                        socket.getOutputStream().flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in KeyboardThread: " + e.getMessage());
            } finally {

                try {
                    keyboard.close();
                } catch (IOException e) {
                    // Ignore as we are closing the thread
                }
            }


           // ListeningThread.start();


            //keyboardThread.join();
            listeningThread.join();

        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
