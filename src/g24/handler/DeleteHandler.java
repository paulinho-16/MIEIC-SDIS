package g24.handler;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashSet;

import g24.Chord;
import g24.Identifier;
import g24.storage.*;

public class DeleteHandler implements Runnable {
    private Chord chord;
    private HashSet<Identifier> nextPeers;
    private FileData fileData;
    private Storage storage;
    
    public DeleteHandler(Chord chord , HashSet<Identifier> nextPeers, FileData fileData, Storage storage) {
        this.chord = chord;
        this.nextPeers = nextPeers;
        this.fileData = fileData;
        this.storage = storage;
    }

    @Override
    public void run() {

        for (Identifier node : nextPeers) {
            // Send a restore message and wait for a response containing the file
            byte[] response = this.chord.sendMessage(node.getIp(), node.getPort(), 1000, null, "DELETE", this.fileData.getFileID());

            // Parse Response
            if(response.length == 0) {
                continue;
            }

            String header = new String(response).trim();

            if (header.equals("OK")) {
                try {
                    System.out.println("Peer " + node.getId() + " deleted the file " + this.fileData.getFilename());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}