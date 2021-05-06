package g24.message;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLSocket;

import g24.protocol.*;
import g24.*;

import java.util.Arrays;

public class MessageHandler {

    private ScheduledThreadPoolExecutor scheduler;
    private Chord chord;

    public MessageHandler(ScheduledThreadPoolExecutor scheduler, Chord chord){
        this.scheduler = scheduler;
        this.chord = chord;
    }

    public void handle(SSLSocket socket) throws IOException {
        this.scheduler.execute(this.parse(socket));
    }
    
    private Handler parse(SSLSocket socket) throws IOException {
        
        DataInputStream in = new DataInputStream(socket.getInputStream());
        
        //byte[] data = in.readAllBytes();
        byte[] data = new byte[1000];
        int bytesRead = in.read(data);

        byte[] short_data = new byte[bytesRead];
        System.arraycopy(data, 0, short_data, 0, bytesRead);

        Handler handler = this.prepare(short_data);
        handler.setSocket(socket);

        return handler;
    }

    // Message: <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    private Handler prepare(byte[] message) {
        // BACKUP, DELETE, RESTORE, HELLO, ONLINE, FINDSUCCESSOR
        // BACKUP -> <MessageType> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF>
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
                return new Backup(splitHeader[1], Integer.parseInt(splitHeader[2]), Integer.parseInt(splitHeader[3]));
            case "DELETE":
                return new Delete(splitHeader[1]);
            case "RESTORE": 
                return new Restore(splitHeader[1]);
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
