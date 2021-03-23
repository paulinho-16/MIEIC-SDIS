public class ChunkThread implements Runnable {
    String fileID, senderID;
    int chunkNumber;

    public ChunkThread(String senderID, String fileID, int chunkNumber) {
        this.senderID = senderID;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
        System.out.println("Thread Constructor called");
    }

    @Override
    public void run() {
        // Sending a chunk to the MDR channel
        String chunkID = fileID + "-" + chunkNumber;

        // Checking if the chunk exists
        if (!Peer.getData().hasChunkBackup(chunkID)) {
            System.out.println("Chunk " + chunkNumber + " doesn't exist");
            return;
        }
        Chunk chunk = Peer.getData().getChunkBackup(chunkID);
        // Versão??? tem de estar associada a quê?
        String debug = Integer.toString(chunkNumber);
        byte[] message = MessageParser.makeMessage(chunk.getData(), "1.0", "CHUNK", Peer.getPeerID(), fileID, Integer.toString(chunkNumber));

        System.out.println("ChunkThread sending :: CHUNK chunk " + chunk.getChunkNumber() + " Sender " + Peer.getPeerID());
        Peer.executor.execute(new Thread(() ->
                           Peer.getMDRChannel().sendMessage(message)
                   ));
    }
}
