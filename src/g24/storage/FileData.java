package g24.storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import g24.Identifier;
import g24.Utils;

// Class that stores info about a given file
public class FileData implements Serializable {
    private String filename;
    private File file;
    private String fileID;
    private int replicationDegree;
    private ConcurrentHashMap<Identifier,Boolean> peers;
    private byte[] data;

    public FileData(String filename, int replicationDegree) {
        this.file = new File(filename);
        this.filename = this.file.getName();
        this.fileID = "-1";
        try {
			this.fileID = Utils.generateFileHash(this.file);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
        this.replicationDegree = replicationDegree;
        this.peers = new ConcurrentHashMap<Identifier,Boolean>();
    }

    public FileData(String fileID, byte[] data, int replicationDegree) {
       this.fileID = fileID;
       this.data = data;
       this.replicationDegree = replicationDegree;
    }

    public FileData(String fileID, String filename){
        this.filename = filename;
        this.fileID = fileID;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getFileID() {
        return this.fileID;
    }

    public int getReplicationDegree() {
        return this.replicationDegree;
    }

    public File getFile() {
        return this.file;
    }

    public void addPeer(Identifier id) {
        this.peers.put(id, true);
    }

    public int getTotalPeers() {
        return this.peers.size();
    }

    public ConcurrentHashMap<Identifier,Boolean> getPeers() {
        return this.peers; 
    }

    public byte[] getData() throws IOException {

        if(this.data != null){
            return this.data;
        }

        if(this.getFile() != null){
            byte[] data = Files.readAllBytes(this.getFile().toPath());
            return data;
        }

        return new byte[0];
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}