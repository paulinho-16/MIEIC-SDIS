import java.io.IOException;
import java.net.InetAddress;

public class MulticastDataRecovery extends MulticastChannel{
    MulticastDataRecovery(InetAddress addr, int port, String peerID) throws IOException {
        super(addr, port, peerID);
    }
}