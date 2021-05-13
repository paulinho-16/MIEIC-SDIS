package g24.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import g24.Chord;
import g24.Identifier;
import g24.Utils;
import g24.storage.FileData;
import g24.storage.Storage;

public class RestoreHandler implements Runnable {

    private Chord chord;
    private HashSet<Identifier> nextPeers;
    private FileData fileData;
    private Storage storage;

    public RestoreHandler(Chord chord, HashSet<Identifier> nextPeers, FileData fileData, Storage storage) {
        this.chord = chord;
        this.nextPeers = nextPeers;
        this.fileData = fileData;
        this.storage = storage;
    }

    @Override
    public void run() {
        
        for (Identifier node : nextPeers) {

            // Send a restore message and wait for a response containing the file
            byte[] response = this.chord.sendMessage(node.getIp(), node.getPort(), 1000, null, "RESTORE", this.fileData.getFileID());

            // Parse Header
            int i;  // Breakpoint index for header
            for (i = 0; i < response.length; i++) {
                
                if (i + 3 > response.length) {
                    System.err.println("Invalid Header");
                    return;
                }

                if (response[i] == Utils.CR && response[i + 1] == Utils.LF && response[i + 2] == Utils.CR && response[i + 3] == Utils.LF) {
                    break;
                }
            }

            // Get body from the message
            if(response.length == 0){
                continue;
            }
            
            String header = new String(Arrays.copyOfRange(response, 0, i)).trim();
            byte[] body = Arrays.copyOfRange(response, i+4, response.length);

            System.err.println("Restore header " + header + " " + body.length);

            if (header.equals("OK")) {
                this.fileData.setData(body);
                try {
                    this.storage.storeRestored(this.fileData);
                    System.out.println("Peer " + node.toString() + " sent the file to be restored");
                    return;
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("There are no peers with that file backed up");
    }
}