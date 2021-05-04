package g24.protocol;

import java.io.DataOutputStream;

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

            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeBytes(predecessor.getIp() + " " + predecessor.getPort());
    
            out.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
