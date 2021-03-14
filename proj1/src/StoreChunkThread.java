import java.util.concurrent.TimeUnit;

public class StoreChunkThread implements Runnable {
    private byte[] message;
    private Chunk chunk;
    private int numResends = 1; // Number of times that the message was sent
    private int delay = 1;  // Delay to resend the next message

    public static final int LIMIT_SEND = 5; // Number of times to send PUTCHUNK message

    public StoreChunkThread(byte[] message, Chunk chunk) {
        this.message = message;
        this.chunk = chunk;
    }

    @Override
    public void run() {
        int numReplications = Peer.getData().getBackupChunk(chunk.getFileID(), chunk.getChunkNumber()).getNumReplications();

        // The number of chunk replications is lower than the desired replication degree: resend PUTCHUNK message
        if (numReplications < chunk.getReplicationDegree()) {
            // Verificar se mandamos para o channel errado
            Peer.executor.execute(new Thread(() -> Peer.getMDBChannel().sendMessage(message)));

            if (numResends < LIMIT_SEND)
                Peer.executor.schedule(this, this.delay, TimeUnit.SECONDS);
            //else
                //Peer.getData().getBackupChunk(chunk.getFileID(), chunk.getChunkNumber())
                // O que fazer quando terminar o tempo?

            numResends++;
            delay *= 2;
        }
        else {
            System.out.println("Replication Degree fulfilled");
        }

        // E no caso de receber 2 mensagens do mesmo peer? Contador soma 2 vezes...
    }
}
