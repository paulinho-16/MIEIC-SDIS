import java.io.File;
import java.util.concurrent.CopyOnWriteArraySet;

class Chunk implements java.io.Serializable{
    private String fileId;
    private int chunkNumber;
    private byte[] body;
    private String version;
    private int desiredReplicationDegree;

    // https://howtodoinjava.com/java/collections/java-copyonwritearraylist/
    private CopyOnWriteArraySet<String> backupPeers = new CopyOnWriteArraySet<>();

    public Chunk(String version, String fileId, int chunkNumber, int desiredReplciationDegree, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.desiredReplicationDegree = desiredReplciationDegree;
        this.body = body;
    }

    public byte[] getData() {
        return body;
    }

    public int getSize() {
        return body.length;
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

    public CopyOnWriteArraySet<String> getPeersBackingUp() {
        return backupPeers;
    }

    public boolean delete() {
        File file = new File(Peer.DIRECTORY + Peer.getPeerID() + "/chunks/" + fileId + "-" + chunkNumber);
        return file.delete();
    }
}