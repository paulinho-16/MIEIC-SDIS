package g24.message;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import g24.*;
import g24.storage.Storage;

public class MessageReceiver implements Runnable {
    private SSLServerSocket socket;
    private MessageHandler handler;
    private Chord chord;

    public MessageReceiver(int port, ScheduledThreadPoolExecutor scheduler, Chord chord, Storage storage) throws IOException {

        this.chord = chord;
        this.handler = new MessageHandler(scheduler, this.chord, storage);
        
        // Creating Server Socket
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.socket = (SSLServerSocket) ssf.createServerSocket(port);
        this.socket.setEnabledCipherSuites(Utils.CYPHER_SUITES);
    }

    @Override
    public void run() {
        while(true) {
            try {
                SSLSocket newSocket = (SSLSocket) this.socket.accept();
                newSocket.setEnabledCipherSuites(Utils.CYPHER_SUITES); 
                newSocket.startHandshake();
                this.handler.handle(newSocket);
            } catch(Exception e) {
                Utils.out("ERROR", e.getMessage());
            }
        }
    }
}
