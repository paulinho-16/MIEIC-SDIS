package g24.storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import g24.Utils;

// Class that stores info about a given file
public class FileData implements Serializable {
    private String filename;
    private File file;
    private String fileID;
    private int replicationDegree;
    private ConcurrentHashMap<Integer,Boolean> peers;
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
        this.peers = new ConcurrentHashMap<Integer,Boolean>();
    }

    public FileData(String fileID, byte[] data){
       this.fileID = fileID;
       this.data = data;
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

    public void addPeer(int id){
        this.peers.put(id, true);
    }

    public int getTotalPeers(){
        return this.peers.size();
    }
}