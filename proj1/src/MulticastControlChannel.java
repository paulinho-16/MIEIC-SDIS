import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.io.File;
import java.util.Arrays;

public class MulticastControlChannel extends MulticastChannel{
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException{
        super(addr,port,peerID);
    }


}