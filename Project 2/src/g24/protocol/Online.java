package g24.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public class Online extends Handler {
    @Override
    public void run() {
        try {
            
            byte[] message = ("HELLO DARKNESS MY OLD FRIEND").getBytes();
            out.write(message, 0, message.length);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
