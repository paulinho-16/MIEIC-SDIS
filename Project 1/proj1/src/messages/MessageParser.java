package messages;

import java.util.Arrays;

// Class for Parsing the messages according to the project guide
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

    public int getReplicationDegree() {
        return replicationDeg;
    }

    public int getPort() {
        return port;
    }

    // Message: <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    // <Body> only exists in PUTCHUNK messages, and in the CHUNK messages if in version 1.0
    // Parses the message received
    public boolean parse() {
        int i;  // Breakpoint index for header
        for (i = 0; i < this.message.length; i++) {
            if (i + 3 > this.message.length)
                return false;

            if (this.message[i] == CR && this.message[i + 1] == LF && this.message[i + 2] == CR && this.message[i + 3] == LF) {
                break;
            }
        }

        // Get header from the message
        String header = new String(Arrays.copyOfRange(message, 0, i));  // Get Header from the message
        String[] splitHeader = header.trim().split("\\s+"); // Remove extra spaces and separate header component

        // Parse header parameters
        for(int j = 0; j < splitHeader.length;j++) {
           if(j == 0) this.version = splitHeader[0];
           else if(j == 1) this.messageType = splitHeader[1];
           else if(j == 2) this.senderID = splitHeader[2];
           else if(j == 3) this.fileID = splitHeader[3];
           else if(j == 4) this.chunkNo = Integer.parseInt(splitHeader[4]);
           else if(j == 5 && this.messageType.equals("GETCHUNK") && this.version.equals("2.0")) {
               try { this.port = Integer.parseInt(splitHeader[5].split(CRLF)[0]);} catch(Exception e) {e.printStackTrace();}
           }
           else if(j == 5) this.replicationDeg = Integer.parseInt(splitHeader[5]);

           else return false;
        }

        // Get body from the message
        if (this.messageType.equals("PUTCHUNK") || (this.messageType.equals("CHUNK"))) {
            this.body = Arrays.copyOfRange(message, i + 4, message.length);
        }

        return true;
    }

    // https://stackoverflow.com/questions/5368704/appending-a-byte-to-the-end-of-another-byte
    // Create a message with a body (used on PUTCHUNK, or CHUNK in version 1.0)
    public static byte[] makeMessage(byte[] body, String... headerString) {
        byte[] header = (String.join(" ", headerString) + CRLF + CRLF).getBytes();
        byte[] message = new byte[header.length + body.length];
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(body, 0, message, header.length, body.length);

        return message;
    }

    // Create a message with header only, no body
    public static byte[] makeHeader(String... headerString) {
        return (String.join(" ", headerString) + CRLF + CRLF).getBytes();
    }

    // Create the 2.0 version GETCHUNK message that contains 2 lines separated by CRLF
    public static byte[] makeGetChunkMessage(String port, String... headerString) {
        return (String.join(" ", headerString) + " " + CRLF +
                port + " " + CRLF + CRLF).getBytes();
    }
}