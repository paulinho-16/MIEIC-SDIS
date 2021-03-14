import java.util.HashSet;

class Chunk implements java.io.Serializable{
    private String fileId;
    private int chunkNumber;
    private int replicationDegree = 1;
    private HashSet<Integer> peersWithChunk = new HashSet<>();

    public Chunk(String fileId, int chunkNumber){
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
    }
    public Chunk(String fileId, int chunkNumber, int replicationDegree){
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.replicationDegree = replicationDegree;
    }
}