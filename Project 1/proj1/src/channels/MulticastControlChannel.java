package channels;

import messages.MessageParser;
import peer.Peer;
import storage.Chunk;
import storage.FileData;

import java.io.*;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// MC Channel
public class MulticastControlChannel extends MulticastChannel {
    public MulticastControlChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr,port,peerID);
    }

    // Send the STORED message to the MC Channel
    public void sendStoreMsg(Chunk chunk) {
        System.out.println("MC sending :: STORED chunk " + chunk.getChunkNumber() + " Sender " + this.peerID);

        byte[] message =  MessageParser.makeHeader(chunk.getVersion(), "STORED", this.peerID , chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));

        Peer.executor.execute(new Thread(() -> this.sendMessage(message)));

        // Add self to peers backing up chunk
        Peer.getData().updateChunkReplicationsNum(chunk.getFileID(), chunk.getChunkNumber(), this.peerID);
    }

    // Restore protocol
    public void restore(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String path = Peer.getPersonalFilesPath() + "/" + filename;
        File file = new File(path);
        String fileID = createId(peerID, path, file.lastModified());

        // Check if the file to restore belongs to the current peer
        if (!Peer.getData().hasFileData(fileID)) {
            System.err.println("Error restoring file " + path + ": File does not belong to Peer " + peerID);
            return;
        }

        FileData fileData = Peer.getData().getFileData(fileID);
        int totalChunks = fileData.getChunkNumbers();

        // Request the file chunks, sending GETCHUNK messages for each of them
        for (int chunkNumber = 0;  chunkNumber < totalChunks; chunkNumber++) {
            byte[] message;
            String chunkID = fileID + "-" + chunkNumber;

            // Add chunks to the waiting list, necessary to know when the file is ready to be restored
            Peer.getData().addWaitingChunk(chunkID);

            // GETCHUNK message only contains the body with the chunk data in version 1.0
            if (Peer.getVersion().equals("2.0")) {
                    int port = Peer.port;
                    message = MessageParser.makeGetChunkMessage(Integer.toString(port),Peer.getVersion(), "GETCHUNK", Peer.getPeerID() , fileID, Integer.toString(chunkNumber));
                    Peer.executor.execute(new Thread(() -> sendMessage(message)));
            }
            else {
                message = MessageParser.makeHeader(Peer.getVersion(), "GETCHUNK", peerID , fileID, Integer.toString(chunkNumber));
                Peer.executor.execute(new Thread(() -> sendMessage(message)));
            }

            System.out.println("MC sending :: GETCHUNK Sender " + peerID + " file "+ fileID + "chunk " + chunkNumber);
        }
    }

    // Delete protocol
    public void delete( String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String path = Peer.getPersonalFilesPath() + "/" + filename;
        File file = new File(path);

        // Check if the file to delete belongs to the current peer
        if (!file.exists()) {
            System.err.println("File " + filename + " doesn't belong to this peer.");
            return;
        }

        String fileID = this.createId(this.peerID, path, file.lastModified());

        // Peer assumes that every peer backing up the file will delete their chunks in version 1.0, and waits for their responses in version 2.0
        if (Peer.getVersion().equals("1.0")) {
            Peer.getData().resetPeersBackingUp(fileID);
            Peer.getData().deleteFileFromMap(fileID);
        }
        else {
            Peer.getData().addDeletedFile(fileID);
        }

        System.out.println("MC sending :: DELETE Sender " + this.peerID + " file " + fileID);

        // Send the DELETE message to warn the other peers to delete their local copies of chunks of the file to delete
        byte[] message =  MessageParser.makeHeader(Peer.getVersion(), "DELETE", this.peerID , fileID);
        Random random = new Random();
        Peer.executor.schedule(new Thread(() ->
                        this.sendMessage(message)),
                random.nextInt(401), TimeUnit.MILLISECONDS
        );
    }

    // Reclaim protocol
    public void reclaim(int disk_space) {
        // Set the new total space of the peer, in bytes
        Peer.getData().setTotalSpace(disk_space * 1000);

        // Delete chunks until the total space is respected
        if (Peer.getData().allocateSpace()) {
            System.out.println("Reclaim successful: Peer " + Peer.getPeerID() + " now has " + (Peer.getData().getTotalSpace() - Peer.getData().getOccupiedSpace()) + " free space");
        }
        else
            System.err.println("Error reclaiming space on peer " + Peer.getPeerID());
    }
}