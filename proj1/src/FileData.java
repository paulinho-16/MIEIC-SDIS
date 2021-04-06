import java.io.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FileData implements Serializable {
    private final String path;
    private final String fileID;
    private final int replicationDegree;

    // CopyOnWriteArraySet<chunkID>
    private final CopyOnWriteArraySet<String> backupChunks = new CopyOnWriteArraySet<>();

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