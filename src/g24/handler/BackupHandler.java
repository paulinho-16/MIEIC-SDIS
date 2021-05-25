package g24.handler;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import g24.Chord;
import g24.Identifier;
import g24.storage.*;
import g24.Utils;

public class BackupHandler implements Runnable {
    private Chord chord;
    private Storage storage;
    private String filename;
    private int replicationDegree;
    private FileData fileData;
    private Identifier backupNode;
    private Identifier startingNode;
    private boolean leaving;
    
    public BackupHandler(Chord chord, Storage storage, String filename, int replicationDegree) {
        this.chord = chord;
        this.storage = storage;
        this.filename = filename;
        this.replicationDegree = replicationDegree;
        
        try {
            this.fileData = new FileData(this.filename, replicationDegree);
            Identifier fileKey = new Identifier(Utils.generateHash(fileData.getFileID()));
            this.backupNode = this.chord.findSuccessor(fileKey);
            this.startingNode = this.backupNode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.leaving = false;
    }

    public BackupHandler(Chord chord, Storage storage, FileData fileData, Identifier backupNode, int replicationDegree) {
        this.chord = chord;
        this.storage = storage;
        this.fileData = fileData;
        try {
            this.backupNode = backupNode;
            Identifier fileKey = new Identifier(Utils.generateHash(fileData.getFileID()));
            this.startingNode = this.chord.findSuccessor(fileKey);
            this.replicationDegree = replicationDegree;
            this.leaving = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
		try {
            HashSet<Identifier> nextPeers = new HashSet<Identifier>();
            Identifier successor = new Identifier(this.backupNode.getIp(), this.backupNode.getPort());
            int count = this.replicationDegree;
            while (nextPeers.size() < this.replicationDegree) {
                    
                if(successor.equals(this.chord.getId())) {    
                    if(!this.leaving) {
                        if(this.storage.store(new FileData(this.fileData.getFileID(), this.fileData.getData(), count))) {
                            nextPeers.add(successor);
                            count--;
                        }
                    }
                }
                else {
                    byte[] fileBytes = fileData.getData();
                    byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, fileBytes, "BACKUP", this.fileData.getFileID(), Integer.toString(count));
                    String resp = new String(response, StandardCharsets.UTF_8);
    
                    if (resp.equals("OK")) {
                        nextPeers.add(successor);
                        count--;
                    }
                }

                successor = new Identifier(successor.getId() + 1);
                successor = this.chord.findSuccessor(successor);
                if(successor.equals(this.backupNode) || (this.leaving && successor.equals(this.startingNode)))
                    break;
            }
            
            // Send delete
            if(count == 0 && !successor.equals(this.chord.getId()) && !(this.replicationDegree > 0 && successor.equals(this.backupNode))) {
                byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, null, "DELETE", this.fileData.getFileID());
            }

            System.err.println("Replication degree: " + nextPeers.size() + " out of " + this.replicationDegree + " desired copies");
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}