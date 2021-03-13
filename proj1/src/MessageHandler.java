public class MessageHandler implements Runnable {
    private byte[] message;
    private MessageParser messageParser;

    public MessageHandler(byte[] message) {
        this.message = message;
        this.messageParser = new MessageParser(message);
    }

    @Override
    public void run() {
        if (this.messageParser.parse() < 0) {
            System.out.println("Error parsing message");
            return;
        }
    }
}