package g24.handler;
import g24.Chord;
import g24.Identifier;
import g24.storage.FileData;
import java.net.SocketTimeoutException;
import java.nio.charset.*;

public class BackupExtraHandler implements Runnable {
    private Chord chord;
    private Identifier sender;
    private FileData fileData;
    private int replicationDegree;
    
    public BackupExtraHandler(Identifier sender, FileData fileData, Integer replicationDegree, Chord chord) {
        this.chord = chord;
        this.sender = sender;
        this.fileData = fileData;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
		try {
            byte[] response = this.chord.sendMessage(this.sender.getIp(), this.sender.getPort(), 2000, null, "BACKUPEXTRA", fileData.getFileID(), Integer.toString(this.replicationDegree));
            String resp = new String(response, StandardCharsets.UTF_8);

            String[] respSplit = resp.split(" ");

            if (respSplit[0].equals("OK")) {
                for (int i = 1; i < respSplit.length; i++)
                    this.fileData.addPeer(new Identifier(Integer.parseInt(respSplit[i])));
            }
		}
        catch (Exception e) {
			e.printStackTrace();
		}
    }
}
