import java.io.File;
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
        // TODO
    }

    public void delete(String version, String path) {

        if(path == null) {
            throw new IllegalArgumentException("Invalid filepath");
        }

        File file = new File(path);
        String fileID = this.createId(this.peerID, path, file.lastModified());
        Peer.getData().deleteFileFromMap(fileID);

        System.out.println("MC sending :: DELETE Sender " + this.peerID + " file " + fileID);

        byte[] message =  MessageParser.makeHeader(version, "DELETE", this.peerID , fileID);
        Random random = new Random();
        this.threads.schedule(new Thread(() ->
                        this.sendMessage(message)),
                random.nextInt(401), TimeUnit.MILLISECONDS
        );
    }
}