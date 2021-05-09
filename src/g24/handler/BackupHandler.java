package g24.handler;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import g24.Chord;
import g24.Identifier;
import g24.storage.FileData;

public class BackupHandler implements Runnable {
    private Chord chord;
    private Identifier receiver;
    private FileData fileData;
    
    public BackupHandler(Chord chord, Identifier receiver, FileData fileData){
        this.chord = chord;
        this.receiver = receiver;
        this.fileData = fileData;
    }

    @Override
    public void run() {
		try {
			byte[] fileBytes = Files.readAllBytes(this.fileData.getFile().toPath());
            byte[] response = this.chord.sendMessage(receiver.getIp(), receiver.getPort(), 1000, fileBytes, "BACKUP", fileData.getFileID());
            String resp = new String(response, StandardCharsets.UTF_8);

            if (resp.equals("OK")) {
                this.fileData.addPeer(this.receiver.getId());
            }
		} 
        catch (SocketTimeoutException e) {
            // No response from peer
        }
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}
