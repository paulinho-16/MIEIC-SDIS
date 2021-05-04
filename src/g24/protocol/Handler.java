package g24.protocol;

import javax.net.ssl.SSLSocket;

public class Handler implements Runnable {
    
    protected SSLSocket socket;

    public void setSocket(SSLSocket socket) {
        this.socket = socket;
    }

    @Override  
    public void run() {
        
    }

}
