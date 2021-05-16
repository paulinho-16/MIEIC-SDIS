package g24.handler;

import java.nio.charset.StandardCharsets;

import g24.Chord;
import g24.Identifier;
import g24.storage.FileData;

public class BackupHandler implements Runnable {
    private Chord chord;
    private Identifier receiver;
    private FileData fileData;
    private int replicationDegree;
    
    public BackupHandler(Chord chord, Identifier receiver, FileData fileData, int replicationDegree) {
        this.chord = chord;
        this.receiver = receiver;
        this.fileData = fileData;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
		try {
			byte[] fileBytes = this.fileData.getData();
            byte[] response = this.chord.sendMessage(this.receiver.getIp(), this.receiver.getPort(), 1000, fileBytes, "BACKUP", this.fileData.getFileID(), Integer.toString(this.replicationDegree));
            String resp = new String(response, StandardCharsets.UTF_8);

            if (resp.equals("OK")) {
                this.fileData.addPeer(this.receiver);
            }
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}
