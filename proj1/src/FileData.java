import java.io.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FileData implements Serializable {
    private String path;
    private String fileID;
    private int replicationDegree;

    // ConcurrentHashMap<ChunkNo, repDegree>
    // CopyOnWriteArraySet<chunkID>
    private CopyOnWriteArraySet<String> backupChunks = new CopyOnWriteArraySet<>();

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

    public CopyOnWriteArraySet<String> getBackupChunks() {
        return backupChunks;
    }

    public void addChunk(String chunkID) {
        backupChunks.add(chunkID);
    }

    public int getChunkNumbers() {
       return backupChunks.size();
    }
}