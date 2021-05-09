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
    //private String[] cypher_suites;

    public MessageReceiver(int port, ScheduledThreadPoolExecutor scheduler, Chord chord, Storage storage) throws IOException {

        this.chord = chord;
        this.handler = new MessageHandler(scheduler, this.chord, storage);
        
        // Creating Server Socket
        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.socket = (SSLServerSocket) ssf.createServerSocket(port);
        this.socket.setEnabledCipherSuites(Utils.CYPHER_SUITES);
        // ArrayList<String> cypher_suites = new ArrayList<String>(Arrays.asList(ssf.getSupportedCipherSuites()));
        // this.cypher_suites = cypher_suites.toArray(new String[cypher_suites.size()]);
    }

    @Override
    public void run() {
        while(true) {
            try {
                SSLSocket newSocket = (SSLSocket) this.socket.accept();
                newSocket.startHandshake();
                newSocket.setEnabledCipherSuites(Utils.CYPHER_SUITES); 
                this.handler.handle(newSocket);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
