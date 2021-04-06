import java.io.File;

class Chunk implements java.io.Serializable{
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

    public boolean delete() {
        File file = new File(Peer.DIRECTORY + Peer.getPeerID() + "/chunks/" + fileId + "-" + chunkNumber);
        return file.delete();
    }
}