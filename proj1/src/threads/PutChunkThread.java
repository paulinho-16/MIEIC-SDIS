package threads;

import peer.Peer;

import java.util.concurrent.TimeUnit;

public class PutChunkThread implements Runnable {
    private final byte[] message;
    private final String chunkID;
    private final int replicationDegree;
    private int numResends = 1; // Number of times that the message was sent
    private int delay = 1;  // Delay to resend the next message

    public static final int LIMIT_SEND = 5; // Number of times to send PUTCHUNK message

    public PutChunkThread(byte[] message, String fileID, int chunkNumber, int replicationDegree) {
        this.message = message;
        this.replicationDegree = replicationDegree;
        this.chunkID = fileID + "-" + chunkNumber;
    }

    @Override
    public void run() {
        int numReplications = Peer.getData().getChunkReplicationNum(chunkID);

        // The number of chunk replications is lower than the desired replication degree: resend PUTCHUNK message
        if (numReplications < replicationDegree) {
            System.out.println("MDB sending :: PUTCHUNK chunk " + chunkID + " Sender " + Peer.getPeerID() + " NumSend " + numResends + " Delay " + delay);
            Peer.executor.execute(new Thread(() ->
                Peer.getMDBChannel().sendMessage(message)
            ));

            if (numResends < LIMIT_SEND)
                Peer.executor.schedule(this, this.delay, TimeUnit.SECONDS);

            numResends++;
            delay *= 2;
        }
        else {
            System.out.println("Fulfilled replication Degree for chunk " + chunkID);
        }
    }
}
