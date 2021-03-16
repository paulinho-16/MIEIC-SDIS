import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

class Chunk implements java.io.Serializable{
    private String fileId;
    private int chunkNumber;
    private int replicationDegree = 1;
    private byte[] body;
    private HashSet<Integer> peersWithChunk = new HashSet<>();
    private String version;

    // https://howtodoinjava.com/java/collections/java-copyonwritearraylist/
    private CopyOnWriteArrayList<String> backupPeers = new CopyOnWriteArrayList<>();

    public Chunk(String fileId, int chunkNumber) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
    }

    public Chunk(String version, String fileId, int chunkNumber, int replicationDegree, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.replicationDegree = replicationDegree;
        this.body = body;
        System.out.println(this.body.length);
    }

    public byte[] getData() {
        return body;
    }

    public int getReplicationDegree() {
        return peersWithChunk.size();   // Variável replicationDegree?
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
}