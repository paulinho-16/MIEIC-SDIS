import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

class Chunk implements java.io.Serializable{
    private String fileId;
    private int chunkNumber;
    private byte[] body;
    private String version;

    // https://howtodoinjava.com/java/collections/java-copyonwritearraylist/
    private CopyOnWriteArrayList<String> backupPeers = new CopyOnWriteArrayList<>();

    public Chunk(String version, String fileId, int chunkNumber, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.body = body;
    }

    public byte[] getData() {
        return body;
    }

    public String getVersion() {
        return version;
    }

    public String getFileID() {
        return this.fileId;
    }

    public int getChunkNumber() {
        return this.chunkNumber;
    }

    public String getID() {
        return this.fileId + "-" + this.chunkNumber;
    }

    public int getNumReplications() {
        return this.backupPeers.size();
    }

    public boolean isPeerBackingUp(String senderID) {
        return backupPeers.contains(senderID);
    }

    public void addPeerBackingUp(String peerID) {
        this.backupPeers.add(peerID);
    }
}