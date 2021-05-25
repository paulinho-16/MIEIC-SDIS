package g24.storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import g24.Utils;

// Class that stores info about a given file
public class FileData implements Serializable {
    private String filename;
    private File file;
    private FileKey fileKey;
    private byte[] data;

    public FileData(String filename, int replicationDegree) {
        this.file = new File(filename);
        this.filename = this.file.getName();
        this.fileKey = new FileKey();

        String fileID = "-1";
        try {
			fileID = Utils.generateFileHash(this.file);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

        this.fileKey = new FileKey(fileID, replicationDegree);
        
    }

    public FileData(String fileID, byte[] data, int replicationDegree) {
        this.fileKey = new FileKey(fileID, replicationDegree);
        this.data = data;
        this.fileKey.setSize(this.data.length);
    }

    public FileData(String fileID, String filename) {
        this.filename = filename;
        this.fileKey = new FileKey(fileID, -1);
    }

    public String getFilename() {
        return this.filename;
    }

    public String getFileID() {
        return this.fileKey.getFileID();
    }

    public int getReplicationDegree() {
        return this.fileKey.getReplicationDegree();
    }

    public File getFile() {
        return this.file;
    }

    public byte[] getData() throws IOException {

        if(this.data != null) {
            return this.data;
        }

        if(this.getFile() != null) {
            byte[] data = Files.readAllBytes(this.getFile().toPath());
            return data;
        }

        return new byte[0];
    }

    public long getSize() {
        return this.fileKey.getSize();
    }

    public void setData(byte[] data) {
        this.data = data;
        this.fileKey.setSize(this.data.length);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setReplicationDegree(int repDegree){
        this.fileKey.setReplicationDegree(repDegree);
    }
}