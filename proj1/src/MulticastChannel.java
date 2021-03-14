import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MulticastChannel implements Runnable {
    private final InetAddress addr;
    private final int port;
    private final DatagramSocket unicastSocket;
    private final MulticastSocket multicastSocket;
    protected ScheduledThreadPoolExecutor threads;
    private final String peerID;

    MulticastChannel(InetAddress addr, int port, String peerID) throws IOException {
        this.addr = addr;
        this.port = port;
        this.peerID = peerID;
        this.threads = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(200);  // 200 available threads
        this.unicastSocket = new DatagramSocket(port); // Qual socket usar para sendMessage??? Não devia ser o do MC channel?
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(this.addr);
    }

    void sendMessage(byte[] message) {
        try {
            DatagramPacket packetSend = new DatagramPacket(message, message.length, this.addr, this.port);
            unicastSocket.send(packetSend);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error sending message to Multicast Data Channel (MDB)");
        }
    }

    @Override
    public void run() {
        byte[] msgReceived = new byte[65507]; // maximum data size for UDP packet -> https://en.wikipedia.org/wiki/User_Datagram_Protocol

        try {
            while(true) {
                // Waiting to receive a packet
                DatagramPacket requestPacket = new DatagramPacket(msgReceived, msgReceived.length);
                this.multicastSocket.receive(requestPacket);

                Peer.executor.execute(new MessageHandler(requestPacket.getData(), this.peerID));
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error receiving message from Multicast Data Channel (MDB)");
        }
    }

    void receiveMessage(){
        // TODO
    }
}