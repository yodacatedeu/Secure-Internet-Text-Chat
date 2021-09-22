// The seperate client thread that will recieve and display messages form the server.

import java.io.BufferedReader;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

public class Receiver implements Runnable {
    
    private final SSLSocket sock; // the socket connected to the server
    private final BufferedReader bin; // the buffered reader to recieve messages from the server
    
    public Receiver(SSLSocket s, BufferedReader b) {
        sock = s;
        bin = b;
    }

    @Override
    public void run() {
        try {
            
            // While still connected, print the lines of text recieved from the server
            String line;
            while ((line = bin.readLine()) != null)
                System.out.println(line);
            
        // This error is thrown when connection to the server is unexpectedly lost
        } catch (IOException ex) {
            System.err.println("Connection to server lost.");
        }
        // Close the socket then exit
        try {
            sock.close();
        } catch (IOException ex) {
            System.err.println("Error closing socket.");
        }
        // Force exit program (neccesary in the case connection is lost to the 
        // server, since the main thread will still be waiting on keyboard input)
        System.exit(0);
    }
    
}
