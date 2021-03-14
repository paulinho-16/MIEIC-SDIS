import java.io.IOException;
import java.net.InetAddress;

public class MulticastControlChannel extends MulticastChannel{
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException{
        super(addr,port,peerID);
    }

    public void restore(String path) {
        // TO DO
    }

    public void delete(String path) {
        // TO DO
    }
}