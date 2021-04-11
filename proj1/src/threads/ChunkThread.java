package threads;

import messages.MessageParser;
import peer.Peer;
import storage.Chunk;

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

    // Send the chunk requested, if the current peer has a local copy of it and no other peer already sent it
    @Override
    public void run() {
        String chunkID = fileID + "-" + chunkNumber;

        // Checking if the current peer has a local copy of the chunk requested
        if (!Peer.getData().hasChunkBackup(chunkID)) {
            System.err.println("Chunk " + chunkID + " doesn't exist");
            return;
        }

        // Checking if the chunk was already sent by another peer
        if (Peer.getData().hasChunkMessagesSent(chunkID)) {
            System.err.println("Chunk " + chunkID + " has already been sent to the Peer Initiator by another peer");
            return;
        }

        Chunk chunk = Peer.getData().getChunkBackup(chunkID);
        byte[] message;

        // 1.0 version uses UDP Multicast only
        if (Peer.getVersion().equals("1.0")) {
            message = MessageParser.makeMessage(chunk.getData(), chunk.getVersion(), "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));
            Peer.executor.execute(new Thread(() -> Peer.getMDRChannel().sendMessage(message)));
        }
        // 2.0 version uses TCP to send the whole CHUNK message, and UDP to send the header only
        else if (Peer.getVersion().equals("2.0")) {
            message = MessageParser.makeMessage(chunk.getData(), chunk.getVersion(), "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));
            // Using TCP instead
            try {
                // Start Connection
                Socket clientSocket = new Socket(ipAddress.toString().split("/")[1], port);

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                // Send CHUNK message through TCP
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
                System.err.println("Error on sending TCP CHUNK message");
            }
        }

        System.out.println("ChunkThread sending :: CHUNK chunk " + chunk.getChunkNumber() + " Sender " + Peer.getPeerID());
    }
}
