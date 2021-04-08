import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPThread implements Runnable {
    private final ServerSocket serverSocket;

    public TCPThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        try {
            // Initializing the socket
            Socket clientSocket = this.serverSocket.accept();

            // Reading an object
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());

            int count = in.available();
            byte[] data = new byte[Peer.CHUNK_SIZE]; // 64 * 1024 bytes
            int number = in.read(data);

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

            // Ending TCP connection
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
