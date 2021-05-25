package g24.storage;

import java.io.Serializable;

public class FileKey implements Serializable {
    private String fileID;
    private int replicationDegree;
    private int size;

    public FileKey(){
        this.fileID = "-1";
        this.replicationDegree = -1;
        this.size = 0;
    }

    public FileKey(String fileID, int replicationDegree){
        this.fileID = fileID;
        this.replicationDegree = replicationDegree;
        this.size = 0;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getFileID(){
        return this.fileID;
    }

    public int getReplicationDegree(){
        return this.replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
