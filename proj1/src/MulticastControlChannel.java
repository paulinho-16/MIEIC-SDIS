import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MulticastControlChannel extends MulticastChannel {
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr,port,peerID);
    }

    public void sendStoreMsg(Chunk chunk) {
        System.out.println("MC sending :: STORED chunk " + chunk.getChunkNumber() + " Sender " + this.peerID);

        byte[] message =  MessageParser.makeHeader(chunk.getVersion(), "STORED", this.peerID , chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));
        Random random = new Random();
        this.threads.schedule(new Thread(() ->
            this.sendMessage(message)),
            random.nextInt(401), TimeUnit.MILLISECONDS
        );
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

        Enumeration<Integer> chunkNumbers = fileData.getChunkNumbers();

        while(chunkNumbers.hasMoreElements()) {
            Integer chunkNumber = chunkNumbers.nextElement();

            // MUDAR VERSÂO - versão deve estar associada a quê?
            byte[] message =  MessageParser.makeHeader("1.0", "GETCHUNK", peerID , fileID, Integer.toString(chunkNumber));

            String chunkID = fileID + "-" + chunkNumber;
            Peer.getData().addReceivedChunk(chunkID);

            threads.execute(new Thread(() -> sendMessage(message)));

            System.out.println("MC sending :: GETCHUNK Sender " + peerID + " file "+ fileID + "chunk " + chunkNumber);
        }

        // Meter delay???
        Peer.executor.execute(new GetChunkThread(path, fileID, peerID, chunkNumbers));
    }

        public void delete(String version, String path) {

        if(path == null) {
            throw new IllegalArgumentException("Invalid filepath");
        }

        File file = new File(path);
        String fileID = this.createId(this.peerID, path, file.lastModified());
        Peer.getData().deleteFileFromMap(fileID);

        System.out.println("MC sending :: DELETE Sender " + this.peerID + " file " + fileID);

        byte[] message =  MessageParser.makeHeader(version, "DELETE", this.peerID , fileID);
        Random random = new Random();
        this.threads.schedule(new Thread(() ->
                        this.sendMessage(message)),
                random.nextInt(401), TimeUnit.MILLISECONDS
        );
    }
}