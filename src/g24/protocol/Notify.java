package g24.protocol;

import java.io.DataOutputStream;
import g24.*;

public class Notify extends Handler {
    Identifier node;
    Chord chord;

    public Notify(String ip, int port, Chord chord) {
        this.node = new Identifier(ip, port);
        this.chord = chord;
    }

    @Override
    public void run() {
        try {
            this.chord.notify(this.node);
            
            // System.out.println("SENDING: NOTIFIED");
            byte[] message = ("NOTIFIED").getBytes();
            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
