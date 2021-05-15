package g24.protocol;

import g24.storage.Storage;

public class Delete extends Handler {
    String fileID;
    Storage storage;

    public Delete(String fileID, Storage storage) {
        this.fileID = fileID;
        this.storage = storage;
    }

    @Override  
    public void run() {

        try {
            byte[] message;
            
            if(this.storage.hasFileStored(this.fileID) && this.storage.removeFileData(fileID))
                message = ("OK").getBytes();
            else
                message = ("NOT_FOUND").getBytes();

            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
