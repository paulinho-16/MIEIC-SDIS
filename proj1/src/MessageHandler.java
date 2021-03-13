public class MessageHandler implements Runnable {
    private byte[] message;
    private MessageParser messageParser;

    public MessageHandler(byte[] message) {
        this.message = message;
        this.messageParser = new MessageParser(message);
    }

    @Override
    public void run() {

        // Checking Parsing
        if (!this.messageParser.parse()) {
            System.out.println("Error parsing message");
            return;
        }

        // Checking Id with the Peer (Needs to have access to the peer)
        //if(messageFactory.senderId.equals())



    }
}