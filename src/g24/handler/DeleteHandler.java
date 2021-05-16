package g24.handler;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashSet;

import g24.Chord;
import g24.Identifier;
import g24.Utils;
import g24.storage.*;

public class DeleteHandler implements Runnable {
    private Chord chord;
    private FileData fileData;
    private Storage storage;
    
    public DeleteHandler(Chord chord , FileData fileData, Storage storage) {
        this.chord = chord;
        this.fileData = fileData;
        this.storage = storage;
    }

    @Override
    public void run() {

        try {
            
            Identifier fileKey = new Identifier(Utils.generateHash(this.fileData.getFilename()));
            Identifier backupNode = this.chord.findSuccessor(fileKey);
            Identifier successor = new Identifier(backupNode.getIp(), backupNode.getPort());

            while (true) {

                int leftToNotify = 0;

                if(!successor.equals(this.chord.getId())){
                    byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, null, "DELETE", this.fileData.getFileID());

                    if(response.length == 0) {
                        continue;
                    }
        
                    String[] header = new String(response).split(" ");
                    
                    if(header.length == 2 && header[0].equals("OK"))
                        leftToNotify = Integer.parseInt(header[1]);
                }
                else {
                    String fileID = this.fileData.getFileID();
                    if(this.storage.hasFileStored(fileID)){
                        leftToNotify = this.storage.getFileData(fileID).getReplicationDegree();
                        this.storage.removeFileData(fileID);
                    }
                }
                
                if(leftToNotify == 0)
                    break;

                successor = new Identifier(successor.getId() + 1);
                successor = this.chord.findSuccessor(successor);
                if (successor.equals(backupNode))
                    break;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}