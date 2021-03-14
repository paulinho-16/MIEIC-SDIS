import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class FileData implements Serializable {
    private String path;
    private String fileID;
    private int replicationDegree;

    private final ConcurrentHashMap<Integer, Chunk> backupChunks = new ConcurrentHashMap<>();

    public FileData(String path, String fileID, int replicationDegree) {
        this.path = path;
        this.fileID = fileID;
        this.replicationDegree = replicationDegree;
    }

    public String getPath() {
        return path;
    }

    public String getFileID() {
        return fileID;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public boolean hasChunkBackup(int chunkNumber) {
        return backupChunks.containsKey(chunkNumber);
    }

    public void addChunkBackup(Chunk chunk) {
        this.backupChunks.put(chunk.getChunkNumber(), chunk);
    }

    public Chunk getChunkBackup(int chunkNumber) {
        return this.backupChunks.get(chunkNumber);
    }
}