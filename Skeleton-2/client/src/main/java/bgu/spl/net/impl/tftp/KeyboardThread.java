//
//import bgu.spl.net.impl.tftp.TftpClientEncoderDecoder;
//import bgu.spl.net.impl.tftp.TftpClientProtocol;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.nio.charset.StandardCharsets;
//
//public class KeyboardThread extends Thread {
//    //private final Socket socket;
//    private final BufferedReader keyboard;
//    private final TftpClientProtocol protocol; // Your protocol class
//    private final TftpClientEncoderDecoder encoderDecoder; // Your encoder/decoder class
//
//    private DataOutputStream out;
//    public KeyboardThread(DataOutputStream out, TftpClientProtocol protocol, TftpClientEncoderDecoder encoderDecoder) throws IOException {
//
//        this.keyboard = new BufferedReader(new InputStreamReader(System.in));
//        this.protocol = protocol;
//        this.encoderDecoder = encoderDecoder;
//        this.out=out;
//    }
//
//    @Override
//    public void run() {
//        try {
//            String userInput;
//            while (!socket.isClosed() && (userInput = keyboard.readLine()) != null) {
//                // The protocol could generate the appropriate packet based on user input
//              //  byte[] packet = userInput.getBytes(StandardCharsets.UTF_8); //TOCHECK
//                if (packet != null) {
//                    out.write(encoderDecoder.encode(userInput));
//                    socket.getOutputStream().flush();
//                }
//            }
//        } catch (IOException e) {
//            System.out.println("Error in KeyboardThread: " + e.getMessage());
//        } finally {
//            try {
//                keyboard.close();
//            } catch (IOException e) {
//                // Ignore as we are closing the thread
//            }
//        }
//    }
//}
