package g24.protocol;

import g24.storage.FileData;
import g24.storage.Storage;

public class Backup extends Handler {
    private String fileID;
    private byte[] data;
    private Storage storage;
    private int replicationDegree;

    public Backup(String fileID, int replicationDegree, byte[] data, Storage storage) {
        this.fileID = fileID;
        this.data = data;
        this.storage = storage;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
        
            try {
                FileData newFileData = new FileData(this.fileID, this.data, this.replicationDegree);
                byte[] message;
                if(this.storage.store(newFileData)) {
                    message = ("OK").getBytes();
                    System.out.println("GUARDEIIIII");
                }
                else{
                    message = ("NOT OK").getBytes();
                }

                out.write(message, 0, message.length);
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
    }
}
