import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FileData implements Serializable {
    private String path;
    private String fileID;
    private int replicationDegree;

    // ConcurrentHashMap<ChunkNo, repDegree>
    private ConcurrentHashMap<Integer, CopyOnWriteArraySet<String>> backupChunks = new ConcurrentHashMap<>();

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

    public int getCurrentReplicationDegree() {
        int curRepDeg = Integer.MAX_VALUE;
        for (Integer key : backupChunks.keySet()) {
            if (curRepDeg > backupChunks.get(key).size()){
                curRepDeg = backupChunks.get(key).size();
            }
        }
        return curRepDeg;
    }

    public boolean hasChunkBackup(int chunkNumber) {
        return backupChunks.containsKey(chunkNumber);
    }

    public void addChunk(int chunkNumber) {
        backupChunks.put(chunkNumber, new CopyOnWriteArraySet<>());
    }

    public void addPeerBackingUp(int chunkNumber, String peerID) {
        CopyOnWriteArraySet<String> peersBackingUp = this.backupChunks.get(chunkNumber);
        peersBackingUp.add(peerID);
        this.backupChunks.put(chunkNumber, peersBackingUp);
    }

    public int getChunkReplicationNum(int chunkNumber) {
        return backupChunks.get(chunkNumber).size();
    }

    public int getBackupChunksSize() {
        return backupChunks.size();
    }

    public Chunk getChunkBackup(int chunkNumber) {
        //return this.backupChunks.get(chunkNumber);
        return null;
    }

    public ConcurrentHashMap<Integer, Chunk> getBackupChunks() {
        //return this.backupChunks;
        return null;
    }

    void removeBackupChunks(){

    }

    Enumeration<Integer> getChunkNumbers() {
        return backupChunks.keys();
    }

    public void removePeerBackingUpChunk(int chunkNo, String senderID) {
        CopyOnWriteArraySet<String> peers = backupChunks.get(chunkNo);
        peers.remove(senderID);
        // Será que é por referência? Não é preciso put?

    }
}