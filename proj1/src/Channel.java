import java.io.*
import java.net.*

public class Channel {
    private final InetAddress addr;
    private final int port;
    private final MulticastSocket socket;

    Channel(addr,port){
        this.addr = addr;
        this.port = port;

        socket = new MulticastSocket(port);
        socket.joinGroup(addr)
    }

    void receiveMessage(){
        // TODO
    }

    void sendMessage(){
        // TODO
    }
}