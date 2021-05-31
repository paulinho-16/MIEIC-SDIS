package g24.handler;

import java.io.IOException;
import java.util.Arrays;

import g24.Chord;
import g24.Identifier;
import g24.Utils;
import g24.storage.FileData;
import g24.storage.Storage;

public class RestoreHandler implements Runnable {

    private Chord chord;
    private FileData fileData;
    private Storage storage;

    public RestoreHandler(Chord chord, FileData fileData, Storage storage) {
        this.chord = chord;
        this.fileData = fileData;
        this.storage = storage;
    }

    @Override
    public void run() {
        try {

            Identifier fileKey = new Identifier(Utils.generateHash(this.fileData.getFileID()));
            Identifier backupNode = this.chord.findSuccessor(fileKey);
            Identifier successor = new Identifier(backupNode.getIp(), backupNode.getPort());

            while (true) {

                // Send a restore message and wait for a response containing the file
                byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, null, "RESTORE",
                        this.fileData.getFileID());

                // Get body from the message
                if (response.length != 0) {
                    // Parse Header
                    int i; // Breakpoint index for header
                    for (i = 0; i < response.length; i++) {

                        if (i + 3 > response.length) {
                            return;
                        }

                        if (response[i] == Utils.CR && response[i + 1] == Utils.LF && response[i + 2] == Utils.CR
                                && response[i + 3] == Utils.LF) {
                            break;
                        }
                    }

                    String header = new String(Arrays.copyOfRange(response, 0, i)).trim();
                    byte[] body = Arrays.copyOfRange(response, i + 4, response.length);

                    if (header.equals("OK")) {
                        this.fileData.setData(body);
                        try {
                            Utils.log("RESTORE", "Peer " + successor.toString() + " sent the file to be restored");
                            this.storage.storeRestored(this.fileData);
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                successor = new Identifier(successor.getId() + 1);
                successor = this.chord.findSuccessor(successor);
                if (successor.equals(backupNode))
                    break;
            }

            System.err.println("There are no peers with that file backed up");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}