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
    
    public BackupHandler(Chord chord, Storage storage, String filename, int replicationDegree) {
        this.chord = chord;
        this.storage = storage;
        this.filename = filename;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
		try {
            FileData fileData = this.storage.getBackupFile(filename, replicationDegree);
            Identifier fileKey = new Identifier(Utils.generateHash(fileData.getFilename()));
            Identifier backupNode = this.chord.findSuccessor(fileKey);
            HashSet<Identifier> nextPeers = new HashSet<Identifier>();
            
            Identifier successor = new Identifier(backupNode.getIp(), backupNode.getPort());
            int count = replicationDegree;
            while (nextPeers.size() < replicationDegree) {
                System.err.println("Size: " +  nextPeers.size());

                if(successor.equals(this.chord.getId())) {                    
                    if(this.storage.store(new FileData(fileData.getFileID(), fileData.getData(), count))){   
                        nextPeers.add(successor);
                        count--;
                        System.err.println("SAVED: " +  successor.getId());
                    }
                }
                else {
                    System.err.println("Sending message");
                    byte[] fileBytes = fileData.getData();
                    byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, fileBytes, "BACKUP", fileData.getFileID(), Integer.toString(count));
                    String resp = new String(response, StandardCharsets.UTF_8);
                    System.err.println("Received response");
    
                    if (resp.equals("OK")) {
                        nextPeers.add(successor);
                        count--;
                        System.err.println("SAVED: " +  successor.getId());
                    }
                }

                successor = new Identifier(successor.getId() + 1);
                successor = this.chord.findSuccessor(successor);
                if(successor.equals(backupNode))
                    break;
            }

            System.err.println("Replication degree: " + nextPeers.size() + " out of " + replicationDegree + " desired copies");
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}