package g24.protocol;

import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import g24.Chord;
import g24.Identifier;
import g24.Utils;
import g24.handler.BackupHandler;
import g24.storage.FileData;
import g24.storage.Storage;

public class BackupExtra extends Handler {
    private String fileID;
    private Storage storage;
    private ScheduledThreadPoolExecutor executor;
    private Chord chord;
    private int replicationDegree;

    public BackupExtra(String fileID, int replicationDegree, Storage storage, ScheduledThreadPoolExecutor executor, Chord chord) {
        this.fileID = fileID;
        this.replicationDegree = replicationDegree;
        this.storage = storage;
        this.executor = executor;
        this.chord = chord;
    }

    @Override
    public void run() {
        FileData fileData = this.storage.getFileData(this.fileID);
        int counter = Math.min(replicationDegree, Utils.m);
        
        HashSet<Identifier> fingers = new HashSet<Identifier>(this.chord.getFingerTable().values());

        // Send the BACKUP message to the peers in the finger table
        for(Identifier receiver : fingers) {
            
            if(counter == 0)
                break;

            if(!this.chord.getId().equals(receiver)) {
                this.executor.execute(new BackupHandler(this.chord, receiver, fileData));
                counter--;
            }
        }

        this.executor.schedule( new Thread(()-> {
                try {
                    String response = "OK ";

                    for(Identifier peer : fileData.getPeers().keySet()){
                        response += peer.toString() + " ";
                    }

                    DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
                    byte[] message = (response).getBytes();
                    out.write(message, 0, message.length);
                    out.flush();
                    out.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }), 2000, TimeUnit.MILLISECONDS);
    }
}
