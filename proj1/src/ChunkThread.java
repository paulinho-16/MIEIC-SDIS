import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ChunkThread implements Runnable {
    String fileID, senderID;
    int chunkNumber, port;
    InetAddress ipAddress;

    public ChunkThread(String senderID, String fileID, int chunkNumber, InetAddress ipAddress, int port) {
        this.senderID = senderID;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public void run() {
        // Sending a chunk to the MDR channel
        String chunkID = fileID + "-" + chunkNumber;

        // Checking if the chunk exists
        if (!Peer.getData().hasChunkBackup(chunkID)) {
            System.out.println("Chunk " + chunkID + " doesn't exist");
            return;
        }

        // Checking if the chunk was already sent by another peer
        if (Peer.getData().hasChunkMessagesSent(chunkID)) {
            System.out.println("Chunk " + chunkID + " has already been sent to the Peer Initiator by another peer");
            return;
        }

        Chunk chunk = Peer.getData().getChunkBackup(chunkID);
        byte[] message;
        if (Peer.getVersion().equals("1.0")) {
            message = MessageParser.makeMessage(chunk.getData(), chunk.getVersion(), "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));
            Peer.executor.execute(new Thread(() -> Peer.getMDRChannel().sendMessage(message)));
        }
        else if (Peer.getVersion().equals("2.0")) {
            message = MessageParser.makeMessage(chunk.getData(), chunk.getVersion(), "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));
            // Using TCP instead
            try {
                // Start Connection
                Socket clientSocket = new Socket(ipAddress.toString().split("/")[1], port);

                //servidor.setSoTimeout(400);

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                // Send Message
                out.write(message, 0, message.length);

                // Stopping connection
                out.flush();
                out.close();
                clientSocket.close();

                // Send multicast message to warn other peers that this chunk has already been sent to initiator
                byte[] multicastMessage = MessageParser.makeHeader(chunk.getVersion(), "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));
                Peer.executor.execute(new Thread(() -> Peer.getMDRChannel().sendMessage(multicastMessage)));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error on sending TCP CHUNK message");
            }
        }

        System.out.println("ChunkThread sending :: CHUNK chunk " + chunk.getChunkNumber() + " Sender " + Peer.getPeerID());
    }
}
