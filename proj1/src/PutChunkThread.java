import java.util.concurrent.TimeUnit;

public class PutChunkThread implements Runnable {
    private byte[] message;
    private String fileID;
    private int chunkNumber;
    private int replicationDegree;
    private int numResends = 1; // Number of times that the message was sent
    private int delay = 1;  // Delay to resend the next message

    public static final int LIMIT_SEND = 5; // Number of times to send PUTCHUNK message

    public PutChunkThread(byte[] message, String fileID, int chunkNumber, int replicationDegree) {
        this.message = message;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {
        System.out.println("Entrou PUTCHUNK thread - CHUNK " + chunkNumber);
        int numReplications = Peer.getData().getBackupChunk(fileID, chunkNumber).getNumReplications();
        //System.out.println("NumReplications: " + numReplications + "    numResend: " + numResends + " REPDEG " + replicationDegree + " Chunk " + chunkNumber + " DELAY " + delay);
        // The number of chunk replications is lower than the desired replication degree: resend PUTCHUNK message
        if (numReplications < replicationDegree) {
            // Verificar se mandamos para o channel errado
            Peer.executor.execute(new Thread(() ->
                Peer.getMDBChannel().sendMessage(message)
            ));

            if (numResends < LIMIT_SEND)
                Peer.executor.schedule(this, this.delay, TimeUnit.SECONDS);
            //else
                //Peer.getData().getBackupChunk(chunk.getFileID(), chunk.getChunkNumber())
                // O que fazer quando terminar o tempo?

            numResends++;
            delay *= 2;
        }
        else {
            System.out.println("Fulfilled replication Degree for chunk " + chunkNumber);
        }

        // E no caso de receber 2 mensagens do mesmo peer? Contador soma 2 vezes...
    }
}
