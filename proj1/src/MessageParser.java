import java.util.Arrays;


public class MessageParser {
    public static final byte CR = 0xD, LF = 0xA;  // ASCII codes for <CRLF>
    private byte[] message;
    private byte[] body;

    // Header Atributes
    String version;
    String msgType;
    String senderID;
    String fileID;
    int chunkNo;
    int replicationDeg;

    public MessageParser(byte[] message) {
        this.message = message;
    }

    // Message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    // <Body> only exists in PUTCHUNK messages
    private boolean parse() {
        int i;  // Breakpoint index for header
        for (i = 0; i < this.message.length; i++) {
            if (i + 3 > this.message.length)
                return false;

            if (this.message[i] == CR && this.message[i + 1] == LF && this.message[i + 2] == CR && this.message[i + 3] == LF) {
                break;
            }
        }

        String header = new String(Arrays.copyOfRange(message, 0, i));  // Get Header from the message
        this.body = Arrays.copyOfRange(message, i+4, message.length); // Get body from the message

        String[] splitHeader = header.trim().split(" "); // Remove extra spaces and separate header components


        this.version = Double.parseDouble(splitHeader[0]);
        this.messageType = splitHeader[1];
        this.senderId = splitHeader[2];
        this.fieldId = splitHeader[3];
        this.chunkNo = Integer.parseInt(splitHeader[4]);
        this.replicationDeg = Integer.parseInt(splitHeader[5]);



    }
}