package g24.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

import g24.*;

public class GetPredecessor extends Handler {
    Chord chord;

    public GetPredecessor(Chord chord) {
        this.chord = chord;
    }

    @Override
    public void run() {

        try {
            Identifier predecessor = this.chord.getId().getPredecessor();
            
            byte[] message;
            if (predecessor.equals(new Identifier())) {
                message = ("NOT_FOUND").getBytes();
            }
            else {
                message = (predecessor.getIp() + " " + predecessor.getPort()).getBytes();
            }
            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
