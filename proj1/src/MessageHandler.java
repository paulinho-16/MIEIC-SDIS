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
        System.out.println("Received GETCHUNK message");
    }

    private void handleCHUNK() {
        System.out.println("Received CHUNK message");
    }

    private void handleDELETE() {
        System.out.println("Received DELETE message");
    }

    private void handleREMOVED() {
        System.out.println("Received REMOVED message");
    }
}