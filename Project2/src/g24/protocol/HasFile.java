package g24.protocol;

import g24.storage.Storage;

public class HasFile extends Handler {
    private String fileID;
    private Storage storage;

    public HasFile(Storage storage, String fileID) {
        this.fileID = fileID;
        this.storage = storage;
    }

    @Override
    public void run() {
        
        try {
            byte[] message;
            if(this.storage.hasFileStored(fileID)) {
                message = ("OK").getBytes();
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
