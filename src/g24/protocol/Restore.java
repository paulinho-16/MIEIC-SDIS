package g24.protocol;

import g24.Utils;
import g24.storage.Storage;

public class Restore extends Handler {
    String fileID;
    Storage storage;

    public Restore(String fileID, Storage storage) {
        this.fileID = fileID;
        this.storage = storage;
    }

    @Override
    public void run() {

        try {
            byte[] message;

            if (this.storage.hasFileStored(this.fileID)) {
                byte[] body = this.storage.read(this.fileID).getData();

                byte[] header = ("OK" + Utils.CRLF + Utils.CRLF).getBytes();
                message = new byte[header.length + body.length];
                System.arraycopy(header, 0, message, 0, header.length);
                System.arraycopy(body, 0, message, header.length, body.length);

            } else {
                message = ("NOT_FOUND" + Utils.CRLF + Utils.CRLF).getBytes();
            }

            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
