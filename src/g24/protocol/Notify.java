package g24.protocol;

import java.io.DataOutputStream;
import g24.*;

public class Notify extends Handler {
    private Identifier node;
    private Chord chord;
    private String messageType;

    public Notify(String messageType, String ip, int port, Chord chord) {
        this.node = new Identifier(ip, port);
        this.chord = chord;
        this.messageType = messageType;
    }

    @Override
    public void run() {
        try {
            
            if (this.messageType.equals("P")) {
                this.chord.notifyPredecessor(this.node);
            }
            else if (this.messageType.equals("S")) {
                this.chord.notifySuccessor(this.node);
            }
            else if (this.messageType.equals("L")) {
                this.chord.getId().setPredecessor(this.node);
            }
            
            byte[] message = ("NOTIFIED").getBytes();
            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
