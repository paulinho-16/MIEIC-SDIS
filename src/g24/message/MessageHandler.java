
package g24.message;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLSocket;

import g24.protocol.*;
import g24.storage.Storage;
import g24.*;

import java.util.Arrays;

public class MessageHandler {

    private ScheduledThreadPoolExecutor scheduler;
    private Chord chord;
    private Storage storage;

    public MessageHandler(ScheduledThreadPoolExecutor scheduler, Chord chord, Storage storage) {
        this.scheduler = scheduler;
        this.chord = chord;
        this.storage = storage;
    }

    public void handle(SSLSocket socket) throws IOException {
        this.scheduler.execute(this.parse(socket));
    }
    
    private Handler parse(SSLSocket socket) throws IOException {

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte[] response = new byte[Utils.FILE_SIZE + 200];
        byte[] aux = new byte[Utils.FILE_SIZE + 200];
        int bytesRead = 0;
        int counter = 0;

        int total = in.readInt();
        while(counter != total) {
            bytesRead = in.read(response);
            System.arraycopy(response, 0, aux, counter, bytesRead);
            counter += bytesRead;
        }

        byte[] result = new byte[counter];
        System.arraycopy(aux, 0, result, 0, counter);
        
        Handler handler = this.prepare(result);
        handler.setSocket(socket, out, in);

        return handler;
    }

    // Message: <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    private Handler prepare(byte[] message) {
        // BACKUP -> <MessageType> <FileId> <Body> <CRLF><CRLF>
        // BACKUPEXTRA -> <MessageType> <FileId> <RepDegree> <CRLF><CRLF>
        // DELETE -> <MessageType> <FileId> <CRLF><CRLF>
        // RESTORE -> <MessageType> <FileId> <CRLF><CRLF>
        // ONLINE -> <MessageType> <CRLF><CRLF>
        // NOTIFY -> <MessageType> <NewNode> <CRLF><CRLF>
        // FINDSUCCESSOR -> <MessageType> <NewNode> <CRLF><CRLF>
        // GETPREDECESSOR -> <MessageType> <CRLF><CRLF>

        // Parse Header
        int i;  // Breakpoint index for header
        for (i = 0; i < message.length; i++) {
            if (i + 3 > message.length)
                return null;

            if (message[i] == Utils.CR && message[i + 1] == Utils.LF && message[i + 2] == Utils.CR && message[i + 3] == Utils.LF) {
                break;
            }
        }

        // Get body from the message
        byte[] body = Arrays.copyOfRange(message, i+4, message.length);

        // Get header from the message
        String header = new String(Arrays.copyOfRange(message, 0, i));  // Get Header from the message
        String[] splitHeader = header.trim().split("\\s+"); // Remove extra spaces and separate header component

        // System.out.println("RECEIVED: " + header);
        // System.out.println("--------------------------------");
        
        // Call the respective handler
        switch(splitHeader[0]) {
            case "BACKUP":
                System.out.println(this.chord.getId().toString() + " RECEIVED " + splitHeader[0]);
                return new Backup(splitHeader[1], body, this.storage);
            case "BACKUPEXTRA":
                return new BackupExtra(splitHeader[1], Integer.parseInt(splitHeader[2]), this.storage, this.scheduler, this.chord);
            case "DELETE":
                return new Delete(splitHeader[1]);
            case "RESTORE": {
                //System.out.println("RECEIVED RESTORE ");
                return new Restore(splitHeader[1], this.storage);
            }
            case "ONLINE":
                return new Online();
            case "NOTIFY":
                return new Notify(splitHeader[1], Integer.parseInt(splitHeader[2]), this.chord);
            case "FINDSUCCESSOR":
                return new FindSuccessor(Integer.parseInt(splitHeader[1]), this.chord);
            case "GETPREDECESSOR":
                return new GetPredecessor(this.chord);
            default:
                System.err.println("Message is not recognized by the parser");
                break;
        }

        System.out.println("Parse failed");

        return null;
    }
}
