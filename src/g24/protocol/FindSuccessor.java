package g24.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

import g24.*;

public class FindSuccessor extends Handler {

    Identifier node;
    Chord chord;

    public FindSuccessor(int node, Chord chord) {
        this.node = new Identifier(node);
        this.chord = chord;
    }

    @Override
    public void run() {
        Identifier successor = chord.findSuccessor(this.node);

        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            byte[] message = (successor.getIp() + " " + successor.getPort()).getBytes();
            out.write(message, 0, message.length);
            out.flush();
            out.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
