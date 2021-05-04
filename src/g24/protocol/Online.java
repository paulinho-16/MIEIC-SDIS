package g24.protocol;

import java.io.DataOutputStream;

import g24.*;

public class Online extends Handler {

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            out.writeBytes("Hello from the other side");
            out.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
