package g24;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static String PROTOCOL_VERSION;
    public static int PEER_ID;

    public final static String CRLF = "\r\n";
    public final static byte CR = 0xD;
    public final static byte LF = 0xA;
    
    public final static int CHUNK_SIZE = 64000;
    public final static int MAX_CHUNKS = 1000000;
    public final static long MAX_FILE = 64000000000l;
    public static final int HEADER_SIZE = 128;
    public static final int PACKET_SIZE = CHUNK_SIZE + HEADER_SIZE;
    public static final long MAX_CAPACITY = 2000000000;

    public final static int MAX_TRIES = 5;
    public final static int WAIT_TIME = 1000;
    public final static int MIN_DELAY = 0;
    public final static int MAX_DELAY = 401;

    public enum Protocol {
        BACKUP,
        RESTORE,
        DELETE,
        RECLAIM
    }

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK,
        DELETE,
        REMOVED,
        DELETED,
        GETDELETE
    }

    public final static int getRandomDelay(){
        return ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY);
    }

    public final static String generateHash(File file) throws IOException, NoSuchAlgorithmException {

        StringBuilder builder = new StringBuilder();
        
        // Get name, modification date and owner of the file
        builder.append(file.getName());
        builder.append(file.lastModified());
        builder.append(Files.getOwner(file.toPath()));

        MessageDigest digest;
        byte[] hash = null;
        
        digest = MessageDigest.getInstance("SHA-256");
        hash = digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
    
    public static void usage(String message) {
        error(message);
        System.err.println("Usage: java Peer <protocol-version> <peer-id> <service-ap> <mc-address> <mc-port> <mdb-address> <mdb-port> <mdr-address> <mdr-port>");
    }

    public static void error(String message) {
        System.err.println("ERROR :: " + message);
    }

    /**
     * Print information about a message received by the peer
     * @param protocol
     * @param messageType
     * @param senderId
     * @param message
     */
    public static void receiveLog(Protocol protocol, MessageType messageType, int senderId, String message) {
        System.out.println(protocol.name() + " :: Peer" + PEER_ID + " received " + messageType.name() + " " + message + " from Peer" + senderId);
    }

    /**
     * Print information about a message sent by the peer
     * @param protocol
     * @param messageType
     * @param message
     */
    public static void sendLog(Protocol protocol, MessageType messageType, String message) {
        System.out.println(protocol.name() + " :: Peer" + PEER_ID + " sent " + messageType.name() + " " + message);
    }

    /**
     * 
     * Print information about a specific protocol
     * @param protocol
     * @param message
     */
    public static void protocolLog(Protocol protocol, String message){
        System.out.println(protocol.name() + " :: Peer" + PEER_ID + " " + message);
    }

    /**
     * 
     * Print information about an error that occurred in a specific protocol
     * @param protocol
     * @param messageType
     * @param message
     */
    public static void protocolError(Protocol protocol, MessageType messageType, String message){
        String str = protocol.name() + " ERROR :: Peer" + PEER_ID;
        if(messageType != null)
            str += " failed to send " + messageType.name(); 
        System.err.println(str + " " + message);
    }
}
