package messages;

import peer.Peer;
import storage.Chunk;
import storage.FileData;
import threads.ChunkThread;
import threads.GetChunkThread;
import threads.PutChunkThread;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Class for handling the messages received through the multicast channels
public class MessageHandler implements Runnable {
    private final MessageParser messageParser;
    private final String peerID;
    private final InetAddress senderAddress;

    public MessageHandler(byte[] message, String peerID, InetAddress senderAddress) {
        this.peerID = peerID;
        this.messageParser = new MessageParser(message);
        this.senderAddress = senderAddress;
    }

    // Parse the message received, calling the respective handler according to the message type
    @Override
    public void run() {
        // Parse the message received
        if (!this.messageParser.parse()) {
            System.err.println("Error parsing message");
            return;
        }

        // Ignore self-messages
        if(messageParser.getSenderID().equals(this.peerID)) {
            return;
        }

        // Call the respective handler
        switch (messageParser.getMessageType()) {
            case "PUTCHUNK" -> handlePUTCHUNK();
            case "STORED" -> handleSTORED();
            case "GETCHUNK" -> handleGETCHUNK();
            case "CHUNK" -> handleCHUNK();
            case "DELETE" -> handleDELETE();
            case "REMOVED" -> handleREMOVED();
            case "DELETED" -> handleDELETED();
            case "HELLO" -> handleHELLO();
            default -> System.err.println("Invalid message type received: " + messageParser.getMessageType());
        }
    }

    // Call the protocol to backup the chunk requested
    private void handlePUTCHUNK() {
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();
        System.out.println("MessageHandler receiving :: PUTCHUNK chunk " + chunkID + " Sender " + this.messageParser.getSenderID());
        Random delay = new Random();
        Peer.executor.schedule(new Thread(() -> {
            Chunk chunk = new Chunk(this.messageParser.getVersion(), this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.messageParser.getReplicationDegree(), this.messageParser.getBody());
            Peer.getData().storeNewChunk(chunk);
            }), delay.nextInt(401), TimeUnit.MILLISECONDS
        );
    }

    // Update the replication number of the chunks, according to the sender of the message
    private void handleSTORED() {
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();
        System.out.println("MessageHandler receiving :: STORED chunk " + chunkID + " Sender " + this.messageParser.getSenderID());
        Peer.getData().updateChunkReplicationsNum(this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.messageParser.getSenderID());
    }

    // Call the protocol to send the chunk requested, if not already sent by another peer
    private void handleGETCHUNK() {
        System.out.println("MessageHandler receiving :: GETCHUNK chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();
        Peer.getData().removeChunkMessagesSent(chunkID);
        Random delay = new Random();
        ChunkThread chunkThread = new ChunkThread(this.messageParser.getSenderID(), this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.senderAddress, this.messageParser.getPort());
        Peer.executor.schedule(chunkThread, delay.nextInt(401), TimeUnit.MILLISECONDS);
    }

    // Update the received chunks if the peer was waiting for the chunk received
    private void handleCHUNK() {
        System.out.println("MessageHandler receiving :: CHUNK chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();

        // If the peer was waiting for the chunk received
        if(Peer.getData().hasWaitingChunk(chunkID)) {
            if (Peer.getVersion().equals("1.0")) {
                Peer.getData().removeWaitingChunk(chunkID);
                int replicationDegree = Peer.getData().getFileReplicationDegree(this.messageParser.getFileID());
                Peer.getData().addReceivedChunk(new Chunk(this.messageParser.getVersion(), this.messageParser.getFileID(), this.messageParser.getChunkNo(), replicationDegree, this.messageParser.getBody()));

                // Check if all chunks of a given file have already been received
                doneReceivedAllChunks(messageParser);
            }
        }
        else {
            // Register that another peer has already sent this chunk
            Peer.getData().addChunkMessagesSent(chunkID);
        }
    }

    // Check if all chunks of a given file have already been received
    public static void doneReceivedAllChunks(MessageParser messageParser) {
        if (Peer.getData().receivedAllChunks(messageParser.getFileID())) {
            FileData filedata = Peer.getData().getFileData(messageParser.getFileID());
            int chunkNumbers = filedata.getChunkNumbers();

            // Launching a Thread that restores the file
            GetChunkThread getChunkThread = new GetChunkThread(filedata.getFilename(), messageParser.getFileID(), chunkNumbers);
            Peer.executor.execute(getChunkThread);
        }
    }

    // Delete the chunks of the file requested to be deleted
    private void handleDELETE() {
        System.out.println("MessageHandler receiving :: DELETE file " + this.messageParser.getFileID() + " Sender " + this.messageParser.getSenderID());

        // Delete the chunk files
        if (!Peer.getData().deleteFileChunks(this.messageParser.getFileID())) {
            System.err.println("Error on operation DELETE");
        }

        // In version 1.0, the current peer assumes that every other peer will delete the file chunks
        if (Peer.getVersion().equals("1.0"))
            Peer.getData().resetPeersBackingUp(this.messageParser.getFileID());

        // Send a Multicast Message signaling the initiator that the file has been deleted
        if (Peer.getVersion().equals("2.0")) {
            // The current peer removes itself from the list of peers backing up the file if it had local copies of chunks of that file
            if (!Peer.getData().removePeerBackingUp(this.messageParser.getFileID(), Peer.getPeerID())) {
                System.out.println("This peer has no chunks of the file " + this.messageParser.getFileID());
                return;
            }

            // Send the DELETED message to alert other peers that the current peer deleted their chunks of the file to delete
            System.out.println("MC sending :: DELETED " + " file " + this.messageParser.getFileID() + " Sender " + Peer.getPeerID());
            byte[] message = MessageParser.makeHeader(Peer.getVersion(), "DELETED", Peer.getPeerID(), this.messageParser.getFileID());
            Random delay = new Random();
            Peer.executor.schedule(new Thread(() -> Peer.getMCChannel().sendMessage(message)), delay.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }

    // In version 2.0, DELETED messages are received in response to the DELETE message
    private void handleDELETED() {
        System.out.println("MessageHandler receiving :: DELETED file " + this.messageParser.getFileID() + " Sender " + this.messageParser.getSenderID());

        if(Peer.getVersion().equals("2.0")) {
            // Remove the sender of the message from the list of peers backing up the file
            Peer.getData().removePeerBackingUp(this.messageParser.getFileID(), this.messageParser.getSenderID());

            // If the file replication degree is finally 0, delete the file from the list of files backed up of the initiator peer
            if (Peer.getData().hasFileData(this.messageParser.getFileID())) {
                int fileRepDegree = Peer.getData().getFileReplicationDegree(this.messageParser.getFileID());
                if (fileRepDegree == 0) {
                    Peer.getData().removeDeletedFile(this.messageParser.getFileID());
                    Peer.getData().deleteFileFromMap(this.messageParser.getFileID());
                }
            }
        }
    }

    // Receive REMOVED messages and send PUTCHUNK message if the current peer has a local copy of the chunk removed and its replication degree is below the desired
    // NOTE: This procedure was made according to the project guide, even though it doesn't work for chunks with replication degree 1 that becomes 0 (no PUTCHUNK will be sent because no peer has a local copy of it)
    private void handleREMOVED() {
        System.out.println("MessageHandler receiving :: REMOVED chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();
        Peer.getData().removePeerBackingUpChunk(chunkID, this.messageParser.getSenderID());

        // Verify if the peer has a local copy of the removed chunk
        if(Peer.getData().hasChunkBackup(chunkID)) {
            Chunk chunk = Peer.getData().getChunkBackup(chunkID);
            int currentRepDegree = Peer.getData().getChunkReplicationNum(chunkID);
            int desiredRepDegree = chunk.getDesiredReplicationDegree();

            // Send a PUTCHUNK message if the chunk replication degree is below the desired
            if (currentRepDegree < desiredRepDegree) {
                byte[] message = MessageParser.makeMessage(chunk.getData(), this.messageParser.getVersion(), "PUTCHUNK", Peer.getPeerID(), this.messageParser.getFileID(), Integer.toString(this.messageParser.getChunkNo()), Integer.toString(desiredRepDegree));
                Random delay = new Random();
                PutChunkThread putChunkThread = new PutChunkThread(message, this.messageParser.getFileID(), this.messageParser.getChunkNo(), desiredRepDegree);
                Peer.executor.schedule(putChunkThread,delay.nextInt(401), TimeUnit.MILLISECONDS);
            }
        }
    }

    // In version 2.0, a peer sends the HELLO message as soon as it is initialized, to alert others to send DELETE messages destined for this peer, that occurred while this peer was not active
    private void handleHELLO() {
        System.out.println("MessageHandler receiving :: HELLO Sender " + this.messageParser.getSenderID());

        if (Peer.getVersion().equals("2.0")) {
            Peer.getData().updateDeletedFiles();
        }
    }
}