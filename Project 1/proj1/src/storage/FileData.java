package storage;

import java.io.*;
import java.util.concurrent.CopyOnWriteArraySet;

// Class that stores info about a given file
public class FileData implements Serializable {
    private final String filename;
    private final String fileID;
    private final int replicationDegree;

    // CopyOnWriteArraySet<chunkID> -> Chunks that compose the file
    private final CopyOnWriteArraySet<String> backupChunks = new CopyOnWriteArraySet<>();

    public FileData(String filename, String fileID, int replicationDegree) {
        this.filename = filename;
        this.fileID = fileID;
        this.replicationDegree = replicationDegree;
    }

    public String getFilename() {
        return filename;
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