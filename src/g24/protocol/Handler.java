package g24.protocol;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

public class Handler implements Runnable {
    
    protected SSLSocket socket;
    protected DataOutputStream out;
    protected DataInputStream in;

    public void setSocket(SSLSocket socket, DataOutputStream out, DataInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    @Override  
    public void run() {
        
    }

}
