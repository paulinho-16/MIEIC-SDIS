public class ChunkThread implements Runnable {
    String fileID, senderID;
    int chunkNumber;
    public ChunkThread(String senderID, String fileID, int chunkNumber) {
        this.senderID = senderID;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
    }

    @Override
    public void run() {
        // Sending a chunk to the MDR channel
        String chunkID = fileID + "-" + chunkNumber;

        // Checking if the chunk exists
        if (Peer.getData().hasChunkBackup(chunkID))
            return;

        Chunk chunk = Peer.getData().getChunkBackup(chunkID);
        // Versão??? tem de estar associada a quê?
        byte[] message = MessageParser.makeMessage(chunk.getData(), "1.0", "CHUNK", senderID, fileID, Integer.toString(this.chunkNumber));

        Peer.executor.execute(new Thread(() ->
                Peer.getMDRChannel().sendMessage(message)
        ));
    }
}
