package g24.protocol;

import java.io.DataOutputStream;
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

        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeBytes(successor.getIp() + " " + successor.getPort());
    
            out.close();
            this.socket.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
