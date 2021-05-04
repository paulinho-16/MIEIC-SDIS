package g24.message;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import g24.*;

public class MessageReceiver {
    private SSLServerSocket socket;
    private MessageHandler handler;
    private Chord chord;

    public MessageReceiver(int port, ScheduledThreadPoolExecutor scheduler, Chord chord) throws IOException {
        // Creating Server Socket 
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.chord = chord;
        this.handler = new MessageHandler(scheduler, chord);
        this.socket = (SSLServerSocket) ssf.createServerSocket(port);
    }

    public void run() {
        while(true) {
            try {
                SSLSocket newSocket = (SSLSocket) this.socket.accept();
                newSocket.startHandshake();
                this.handler.handle(newSocket);
                // c.setEnabledCipherSuites(arr);            
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
