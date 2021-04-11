package channels;

import messages.MessageHandler;
import messages.MessageParser;
import peer.Peer;
import storage.Chunk;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPHandler implements Runnable {
    ServerSocket serverSocket;

    public TCPHandler(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while(true) {
            try {
                new ClientHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                // Reading an object
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                byte[] data = in.readAllBytes();

                MessageParser messageParser = new MessageParser(data);

                // Checking Parsing
                if (!messageParser.parse()) {
                    System.out.println("Error parsing message");
                    return;
                }
    
                String chunkID = messageParser.getFileID() + "-" + messageParser.getChunkNo();
                if(Peer.getData().hasWaitingChunk(chunkID)) {
                    Peer.getData().removeWaitingChunk(chunkID);
                    int replicationDegree = Peer.getData().getFileReplicationDegree(messageParser.getFileID());
                    Peer.getData().addReceivedChunk(new Chunk(messageParser.getVersion(), messageParser.getFileID(), messageParser.getChunkNo(), replicationDegree, messageParser.getBody()));
                }

                // Checking if all chunks have already been received and launch GetChunkThread
                MessageHandler.doneReceivedAllChunks(messageParser);

                // Ending TCP connection
                in.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
