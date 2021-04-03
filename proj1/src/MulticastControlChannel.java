import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastControlChannel extends MulticastChannel {
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr,port,peerID);
    }

    public void sendStoreMsg(Chunk chunk) {
        System.out.println("MC sending :: STORED chunk " + chunk.getChunkNumber() + " Sender " + this.peerID);

        byte[] message =  MessageParser.makeHeader(chunk.getVersion(), "STORED", this.peerID , chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));

        // Antes de fazer o enhancement do Backup
        /*
        Random random = new Random();
        Peer.executor.schedule(new Thread(() ->
            this.sendMessage(message)),
            random.nextInt(401), TimeUnit.MILLISECONDS
        );
        */

        Peer.executor.execute(new Thread(() -> this.sendMessage(message)));

        // Add self to peers backing up chunk
        Peer.getData().updateChunkReplicationsNum(chunk.getFileID(), chunk.getChunkNumber(), this.peerID);
    }

    public void restore(String path) {
        //<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        if(path == null) {
            throw new IllegalArgumentException("Invalid filepath");
        }

        File file = new File(path);
        String fileID = createId(peerID, path, file.lastModified());
        // Verificar tb se tem todos os chunks?? como?
        if (!Peer.getData().hasFileData(fileID)) {
            System.out.println("Error restoring file " + path + ": File does not belong to Peer " + peerID);
            return;
        }

        FileData fileData = Peer.getData().getFileData(fileID);

        int totalChunks = fileData.getChunkNumbers();

        for (int chunkNumber = 0;  chunkNumber < totalChunks; chunkNumber++) {
            byte[] message;
            String chunkID = fileID + "-" + chunkNumber;
            Peer.getData().addWaitingChunk(chunkID);

            if(Peer.getVersion().equals("2.0")) {
                try {
                    System.out.println("Starting GetChunk enhancement");
                    String ipAddress = InetAddress.getLocalHost().getHostAddress();

                    ServerSocket socket = new ServerSocket(0);
                    int port = socket.getLocalPort();

                    // Creating alternative header
                    // Qual porta usar? Diferentes para diferentes chunks?
                    message = MessageParser.makeGetChunkMessage(Integer.toString(port),Peer.getVersion(), "GETCHUNK", Peer.getPeerID() , fileID, Integer.toString(chunkNumber));
                    Peer.executor.execute(new Thread(() -> sendMessage(message)));

                    // raise waiting thread
                    TCPThread tcpThread = new TCPThread(socket);
                    Peer.executor.execute(tcpThread);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error on TCP port usage: Server Side");
                }
            }
            else {
                message = MessageParser.makeHeader(Peer.getVersion(), "GETCHUNK", peerID , fileID, Integer.toString(chunkNumber));
                Peer.executor.execute(new Thread(() -> sendMessage(message)));
            }

            System.out.println("MC sending :: GETCHUNK Sender " + peerID + " file "+ fileID + "chunk " + chunkNumber);
        }

        // Meter delay???
        Peer.executor.execute(new GetChunkThread(path, fileID, peerID, totalChunks));
        //Peer.executor.schedule(new GetChunkThread(path, fileID, peerID, numberChunks), 10, TimeUnit.SECONDS);
    }

    public void delete( String path) {
        if(path == null) {
            throw new IllegalArgumentException("Invalid filepath");
        }

        File file = new File(path);
        String fileID = this.createId(this.peerID, path, file.lastModified());
        Peer.getData().resetPeersBackingUp(fileID);
        Peer.getData().deleteFileFromMap(fileID);

        System.out.println("MC sending :: DELETE Sender " + this.peerID + " file " + fileID);

        byte[] message =  MessageParser.makeHeader(Peer.getVersion(), "DELETE", this.peerID , fileID);
        Random random = new Random();
        Peer.executor.schedule(new Thread(() ->
                        this.sendMessage(message)),
                random.nextInt(401), TimeUnit.MILLISECONDS
        );
    }

    public void reclaim(int disk_space) {
        Peer.getData().setTotalSpace(disk_space * 1000);
        // While there is still not enough space, we need to delete more chunks
        if (Peer.getData().allocateSpace())
            System.out.println("Reclaim successful: Peer " + Peer.getPeerID() + " now has " + (Peer.getData().getTotalSpace() - Peer.getData().getOccupiedSpace()) + " free space");
        else
            System.out.println("Error reclaiming space on peer " + Peer.getPeerID());
    }
}