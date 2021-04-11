package messages;

import java.util.Arrays;

public class MessageParser {
    // Macros
    public static final byte CR = 0xD, LF = 0xA;  // ASCII codes for <CRLF>
    public static final String CRLF = "\r\n";

    // Bytes
    private final byte[] message; // Full Message
    private byte[] body; // Body without the header

    // Header Attributes
    private String version, messageType, senderID, fileID;
    private int chunkNo, replicationDeg, port;

    public MessageParser(byte[] message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getFileID() {
        return fileID;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public byte[] getBody() {
        return body;
    }

    // Message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    // <Body> only exists in PUTCHUNK messages
    public boolean parse() {
        int i;  // Breakpoint index for header
        for (i = 0; i < this.message.length; i++) {
            if (i + 3 > this.message.length)
                return false;

            if (this.message[i] == CR && this.message[i + 1] == LF && this.message[i + 2] == CR && this.message[i + 3] == LF) {
                break;
            }
        }

        // Get body from the message
        this.body = Arrays.copyOfRange(message, i+4, message.length);

        // Get header from the message
        String header = new String(Arrays.copyOfRange(message, 0, i));  // Get Header from the message
        String[] splitHeader = header.trim().split("\s"); // Remove extra spaces and separate header component

        // Parse header parameters
        for(int j = 0; j < splitHeader.length;j++) {
           if(j == 0) this.version = splitHeader[0];
           else if(j == 1) this.messageType = splitHeader[1];
           else if(j == 2) this.senderID = splitHeader[2];
           else if(j == 3) this.fileID = splitHeader[3];
           else if(j == 4) this.chunkNo = Integer.parseInt(splitHeader[4]);
           else if(j == 5 && this.messageType.equals("GETCHUNK") && this.version.equals("2.0")) this.port = Integer.parseInt(splitHeader[5].split(CRLF)[1]);
           else if(j == 5) this.replicationDeg = Integer.parseInt(splitHeader[5]);
           else return false;
        }

        return true;
    }

    // https://stackoverflow.com/questions/5368704/appending-a-byte-to-the-end-of-another-byte
    public static byte[] makeMessage(byte[] body, String... headerString) {
        byte[] header = (String.join(" ", headerString) + CRLF + CRLF).getBytes();
        byte[] message = new byte[header.length + body.length];
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(body, 0, message, header.length, body.length);

        return message;
    }

    public static byte[] makeHeader(String... headerString) {
        return (String.join(" ", headerString) + CRLF + CRLF).getBytes();
    }

    public static byte[] makeGetChunkMessage(String port, String... headerString){
        return (String.join(" ", headerString) + " " + CRLF +
                port + " " + CRLF + CRLF).getBytes();
    }

    public int getReplicationDegree() {
        return replicationDeg;
    }

    public int getPort() {
        return port;
    }
}