package channels;

import messages.MessageHandler;
import peer.Peer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class MulticastChannel implements Runnable {
    protected final InetAddress addr;
    protected final int port;
    protected final MulticastSocket multicastSocket;
    protected final String peerID;

    MulticastChannel(InetAddress addr, int port, String peerID) throws IOException {
        this.addr = addr;
        this.port = port;
        this.peerID = peerID;
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(this.addr);
    }

    // Send a message to the multicast channel
    public void sendMessage(byte[] message) {
        try {
            DatagramPacket packetSend = new DatagramPacket(message, message.length, this.addr, this.port);
            this.multicastSocket.send(packetSend);
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Error sending message to Multicast Data Channel (MDB)");
        }
    }

    // Infinite loop to receive new messages and sending them to MessageHandler
    @Override
    public void run() {
        byte[] msgReceived = new byte[65507]; // maximum data size for UDP packet -> https://en.wikipedia.org/wiki/User_Datagram_Protocol

        try {
            while(true) {
                // Waiting to receive a packet
                DatagramPacket requestPacket = new DatagramPacket(msgReceived, msgReceived.length);
                this.multicastSocket.receive(requestPacket);
                byte[] realData = Arrays.copyOf(requestPacket.getData(), requestPacket.getLength());
                Peer.executor.execute(new MessageHandler(realData, this.peerID, requestPacket.getAddress()));
            }
        } catch(Exception e) {
            System.err.println("Error receiving message from Multicast Data Channel (MDB)");
            e.printStackTrace();
        }
    }

    // Create the file ID based on its owner, name and last modified date
    protected String createId(String peerID, String filename, long dateModified) {
        return sha256(peerID + "//" + filename + "//" + dateModified);
    }

    // https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}