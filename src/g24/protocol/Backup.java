package g24.protocol;

public class Backup extends Handler {
    String fileID;
    int chunkNo;
    int repDegree;

    public Backup(String fileID, int chunkNo, int repDegree) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.repDegree = repDegree;
    }

    @Override
    public void run() {
        
    }
}
