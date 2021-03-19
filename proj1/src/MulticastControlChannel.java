import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastControlChannel extends MulticastChannel {
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr,port,peerID);
    }

    public void sendStoreMsg(Chunk chunk) {
        System.out.println("MC sending :: STORED chunk " + chunk.getChunkNumber() + " Sender " + this.peerID);

        byte[] message =  MessageParser.makeHeader(chunk.getVersion(), "STORED", this.peerID , chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));
        Random random = new Random();
        this.threads.schedule(new Thread(() ->
            this.sendMessage(message)),
            random.nextInt(401), TimeUnit.MILLISECONDS
        );
    }

    public void restore(String path) {
        // TO DO
    }

    public void delete(String path) {
        // TO DO
    }
}