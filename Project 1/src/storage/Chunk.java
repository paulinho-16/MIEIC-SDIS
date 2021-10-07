package storage;

import peer.Peer;

import java.io.File;

// Class that stores info about a given chunk
public class Chunk implements java.io.Serializable {
    private final String fileId;
    private final int chunkNumber;
    private final byte[] body;
    private final String version;
    private final int desiredReplicationDegree;

    public Chunk(String version, String fileId, int chunkNumber, int desiredReplicationDegree, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.desiredReplicationDegree = desiredReplicationDegree;
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

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    // Delete the file from the directory
    public boolean delete() {
        File file = new File(Peer.getChunksPath() + "/" + fileId + "-" + chunkNumber);
        return file.delete();
    }
}