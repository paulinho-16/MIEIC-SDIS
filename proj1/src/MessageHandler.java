public class MessageHandler implements Runnable {
    private MessageParser messageParser;
    private Peer peer;

    public MessageHandler(byte[] message, Peer peer) {
        this.peer = peer;
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
        if(messageParser.getSenderID().equals(this.peer.getID())) {
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
        System.out.println("Received PUTCHUNK message");

        this.peer.executor.schedule(new Thread(() -> {
            
        }))

    }

    private void handleSTORED() {
        System.out.println("Received STORED message");
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