package g24.handler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import g24.Chord;
import g24.Identifier;
import g24.storage.*;

public class ReclaimHandler implements Runnable {

    private Chord chord;
    private Storage storage;
    

    public ReclaimHandler(Chord chord,  Storage storage, long diskSpace) {
        this.chord = chord;
        this.storage = storage;
        this.storage.setTotalSpace(diskSpace);
    }
    
    @Override
    public void run() {
        
        Iterator<Map.Entry<String, FileData>> itr = this.storage.getStoredFiles().entrySet().iterator();

        try {
            while (this.storage.overflows() && itr.hasNext()) {
                if (!deleteFile(itr))
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean deleteFile(Iterator<Map.Entry<String, FileData>> itr) throws IOException {
        String fileID = itr.next().getKey();
        FileData fileData = this.storage.getFileData(fileID);
        byte[] fileBytes = fileData.getData();

        int replicationDegree = fileData.getReplicationDegree();
        int count = replicationDegree;
        HashSet<Identifier> nextPeers = new HashSet<Identifier>();
        Identifier successor = this.chord.getId().getSuccessor();

        // Attempt to send the files to other peers, to maintain replication degree
        while (nextPeers.size() < replicationDegree) {

            if(!successor.equals(this.chord.getId())) {

                byte[] response = this.chord.sendMessage(successor.getIp(), successor.getPort(), 1000, fileBytes, "BACKUP", fileData.getFileID(), Integer.toString(count));
                String resp = new String(response, StandardCharsets.UTF_8);

                if (resp.equals("OK")) {
                    nextPeers.add(successor);
                    count--;
                }
            }

            successor = new Identifier(successor.getId() + 1);
            successor = this.chord.findSuccessor(successor);
            if(successor.equals(this.chord.getId()))
                break;
        }

        System.err.println("Replication degree: " + nextPeers.size() + " out of " + replicationDegree + " desired copies");

        return this.storage.removeFileData(fileID);
    }
}
