package peer;

import channels.MulticastControlChannel;
import channels.MulticastDataChannel;
import channels.MulticastDataRecovery;
import threads.StateThread;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PeerProtocol implements PeerInterface {
    // Multicast Channels
    private final MulticastControlChannel mc;
    private final MulticastDataChannel mdb;
    private final MulticastDataRecovery mdr;
    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

    public PeerProtocol(MulticastControlChannel mc, MulticastDataChannel mdb, MulticastDataRecovery mdr) {
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }


    // Call Backup protocol
    @Override
    public void backup(String filepath, int replication_degree) {
        System.out.println("Request: Backup");
        executor.execute(new Thread(() -> {
            try {
                mdb.backup(filepath, replication_degree);
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Error on Backup Protocol");
                e.printStackTrace();
            }
        }));
    }

    // Call Restore protocol
    @Override
    public void restore(String filepath) {
        System.out.println("Request: Restore");
        executor.execute(new Thread(() -> mc.restore(filepath)));
    }

    // Call Delete protocol
    @Override
    public void delete(String filepath) {
        System.out.println("Request: Delete");
        executor.execute(new Thread(() -> mc.delete(filepath)));
    }

    // Call Reclaim protocol
    @Override
    public void reclaim(int disk_space) {
        System.out.println("Request: Reclaim");
        executor.execute(new Thread(() -> mc.reclaim(disk_space)));
    }

    // Call State protocol
    @Override
    public String state() {
        System.out.println("Request: State");

        StateThread stateThread = new StateThread();
        Thread thread = new Thread(stateThread);
        thread.start();
        String printable;
        try {
            thread.join();
            printable = stateThread.getString();
            return printable;
        } catch (InterruptedException e) {
            System.err.println("Error on State Protocol");
            e.printStackTrace();
        }
        return null;
    }
}
