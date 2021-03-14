import java.io.IOException;
import java.net.InetAddress;

public class MulticastControlChannel extends MulticastChannel{
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException{
        super(addr,port,peerID);
    }

    private void backup(String filepath, int replicationDegree){
        if(filepath == null && replicationDegree < 1){
            throw new IllegalArgumentException("Invalid filepath or replicationDegree");
        }

        System.out.println("Initializing ");
    }

}