package g24.protocol;

import g24.*;

public class Notify extends Handler {
    Identifier node;
    Chord chord;

    public Notify(int node, Chord chord) {
        this.node = new Identifier(node);
        this.chord = chord;
    }

    @Override
    public void run() {
        this.chord.notify(this.node);
        try {
            this.socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
