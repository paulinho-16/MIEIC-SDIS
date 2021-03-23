import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MessageHandler implements Runnable {
    private MessageParser messageParser;
    private String peerID;

    public MessageHandler(byte[] message, String peerID) {
        this.peerID = peerID;
        this.messageParser = new MessageParser(message);
    }

    @Override
    public void run() {
        // Checking Parsing
        if (!this.messageParser.parse()) {
            System.out.println("Error parsing message");
            return;
        }

        // Ignore self-messages
        if(messageParser.getSenderID().equals(this.peerID)) {
            return;
        }

        switch (messageParser.getMessageType()) {
            case "PUTCHUNK":
                handlePUTCHUNK();
                break;
            case "STORED":
                handleSTORED();
                break;
            case "GETCHUNK":
                handleGETCHUNK();
                break;
            case "CHUNK":
                handleCHUNK();
                break;
            case "DELETE":
                handleDELETE();
                break;
            case "REMOVED":
                handleREMOVED();
                break;
            default:
                System.out.println("Invalid message type received: " + messageParser.getMessageType());
        }
    }

    private void handlePUTCHUNK() {
        System.out.println("MessageHandler receiving :: PUTCHUNK chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        Random delay = new Random();
        Peer.executor.schedule(new Thread(() -> {
            // Que parámetros serão precisos??? version e senderID??
            Chunk chunk = new Chunk(this.messageParser.getVersion(), this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.messageParser.getBody());
            Peer.getData().storeNewChunk(chunk);
            }), delay.nextInt(401), TimeUnit.MILLISECONDS
        );
    }

    private void handleSTORED() {
        System.out.println("MessageHandler receiving :: STORED chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        Peer.getData().updateChunkReplicationsNum(this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.messageParser.getSenderID());
    }

    private void handleGETCHUNK() {
        System.out.println("MessageHandler receiving :: GETCHUNK chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        Random delay = new Random();
        ChunkThread chunkThread = new ChunkThread(this.messageParser.getSenderID(), this.messageParser.getFileID(), this.messageParser.getChunkNo());
        Peer.getMDRChannel().threads.schedule(chunkThread,delay.nextInt(401), TimeUnit.MILLISECONDS);
    }

    private void handleCHUNK() {
        System.out.println("MessageHandler receiving :: CHUNK chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        String chunkID = this.messageParser.getFileID() + "-" + this.messageParser.getChunkNo();
        if(Peer.getData().hasWaitingChunk(chunkID)) {
            Peer.getData().removeWaitingChunk(chunkID);
            Peer.getData().addReceivedChunk(new Chunk(this.messageParser.getVersion(), this.messageParser.getFileID(), this.messageParser.getChunkNo(), this.messageParser.getBody()));
        }
    }

    private void handleDELETE() {
        System.out.println("MessageHandler receiving :: DELETE chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());

        if (!Peer.getData().deleteFileChunks(this.messageParser.getFileID())) {
            System.out.println("Error on operation DELETE");
        }
    }

    private void handleREMOVED() {
        System.out.println("MessageHandler receiving :: REMOVED chunk " + this.messageParser.getChunkNo() + " Sender " + this.messageParser.getSenderID());
        // Verificar se tem uma cópia local do chunk

        if (Peer.getData().hasFileData(this.messageParser.getFileID())) {
            FileData filedata = Peer.getData().getFileData(this.messageParser.getFileID());
            if (filedata.hasChunkBackup(this.messageParser.getChunkNo())) {
                filedata.removePeerBackingUpChunk(this.messageParser.getChunkNo(), this.messageParser.getSenderID());
                int currentRepDegree = filedata.getCurrentReplicationDegree();
                int desiredRepDegree = filedata.getReplicationDegree();
                System.out.println("Current Degree: " + currentRepDegree);
                System.out.println("Desired Degree: " + desiredRepDegree);
                if (currentRepDegree < desiredRepDegree) {
                    // Versão?? Onde armazenar?
                    System.out.println("ENTROU NO IF MISTERIOSO");
                    System.out.println(this.messageParser.getSenderID());
                    System.out.println(Peer.getPeerID());
                    byte[] message = MessageParser.makeMessage(this.messageParser.getBody(), "1.0", "PUTCHUNK", Peer.getPeerID(), this.messageParser.getFileID(), Integer.toString(this.messageParser.getChunkNo()), Integer.toString(desiredRepDegree));
                    Random delay = new Random();

                    PutChunkThread putChunkThread = new PutChunkThread(message, this.messageParser.getFileID(), this.messageParser.getChunkNo(), desiredRepDegree);
                    Peer.getMCChannel().threads.schedule(putChunkThread,delay.nextInt(401), TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}