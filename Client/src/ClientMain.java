// Main client thread.  Connects to server, starts the reciever thread, then 
// awaits the user's input to send messages to the server/chat service

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientMain {
    
    public static void main(String [] args) throws IOException, InterruptedException {
        Scanner reader = new Scanner(System.in);
        String ip; 
        int port;
        SSLSocket sock;
        boolean connected = false;
        // Load trusted server certificate, then load this client's certificate and public key.
        System.setProperty("javax.net.ssl.trustStore", "servercert.store");
        System.setProperty("javax.net.ssl.keyStore", "clientcert.store");
        System.setProperty("javax.net.ssl.keyStorePassword", "clientlemon");
        
// Loop will continue until a successfull connection is made
        while (!connected) {
            // Input server ip
            System.out.print("IP: ");
            ip = reader.nextLine();
            //ip = "127.0.0.1";  // loopback IP for testing

            // Input port
            System.out.print("Port: ");
            port = Integer.parseInt(reader.nextLine());
            // Try to connect
            try {
                // Create SSL socket 
                SSLSocketFactory sslsf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sock = (SSLSocket) sslsf.createSocket();
//                System.out.println("\nEnabled cipher suites: " + Arrays.toString(sock.getEnabledCipherSuites()));
//                System.out.println("Enabled protocols: " + Arrays.toString(sock.getEnabledProtocols()));
//                System.out.println("Enabled SSL parameters: " + sock.getSSLParameters().toString());
                // Connect socket to inputted endpoint
                System.out.println("\nConnecting to server...");
                SocketAddress endpoint = new InetSocketAddress(ip, port);
                sock.connect(endpoint);
                //System.out.println("Successfull connection: " + sock.isConnected());
                //System.out.println("Socket closed: " + sock.isClosed());
         // Everything below in this block will only happen upon a successfull connection
                connected = true;
                // Create a BufferedReader object for recieving mesages from the server
                BufferedReader bin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                // Create a PrintWiter object to use to send messages to the server
                PrintWriter pout = new PrintWriter(sock.getOutputStream(), true);
                // Unlikely error but will still check
                if (pout.checkError()) {
                    System.err.println("pout error");
                    System.exit(-1);
                }
                
                // Create and start the reciever thread
                Receiver rcvr = new Receiver(sock, bin);
                Thread thrd = new Thread(rcvr);
                thrd.start();
//                if (isSocketClosed(sock)) {
//                    System.out.println("Failed to authenticate with server.  Please exit.");
//                }
                String message = "";
                // Begin main loop allowing user to send lines of text to the server and to exit 
                // by entering "EXIT" or the loop will exit if the socket is closed.
                while (!message.equalsIgnoreCase("EXIT") && !isSocketClosed(sock)) {
                    //System.out.println("Waiting fo input: ");
                    message = reader.nextLine();
                    if (!message.equalsIgnoreCase("EXIT"))
                        pout.println(message);
                    // Send "EXIT" to the server to let it know the user intentionally diconnected
                    else {
                        pout.println("EXIT");
                        //System.out.println("Exiting");
                        // Shutdown the the socket's output stream
                        sock.shutdownOutput();
                        System.out.println("You have successfully disconnected from the server.");
                    }
                }                       
                // Wait for the reciever thread to join then exit program
                thrd.join();
            }
            // Failed to connect to sever case
            catch (ConnectException ex) {
                    System.err.println("Failed to connect to server.  Please recheck credentials and try again.");
            }
        }          
    }
    
    public static synchronized boolean isSocketClosed(SSLSocket sock) {
        return sock.isClosed();
    }
}
