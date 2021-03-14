import java.io.File;
import java.util.HashSet;

class Chunk implements java.io.Serializable{
    private String fileId;
    private int chunkNumber;
    private int replicationDegree = 1;
    private byte[] body;
    private HashSet<Integer> peersWithChunk = new HashSet<>();

    public Chunk(String fileId, int chunkNumber) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
    }

    public Chunk(String fileId, int chunkNumber, int replicationDegree, byte[] body) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    public byte[] getData() {
        File file = new File();
    }

    public int getReplicationDegree() {
        return peersWithChunk.size();   // Variável replicationDegree?
    }

    public String getID() {
        return this.fileId + "-" + this.chunkNumber;
    }
}