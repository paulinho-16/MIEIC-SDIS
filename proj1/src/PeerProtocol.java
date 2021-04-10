import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PeerProtocol implements PeerInterface {
    private final MulticastControlChannel mc;
    private final MulticastDataChannel mdb;
    private final MulticastDataRecovery mdr;
    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

    public PeerProtocol(MulticastControlChannel mc, MulticastDataChannel mdb, MulticastDataRecovery mdr) {
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    @Override
    public void backup(String filepath, int replication_degree) {
        System.out.println("Request: Backup");
        executor.execute(new Thread(() -> {
            try {
                mdb.backup(filepath, replication_degree);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void restore(String filepath) {
        System.out.println("Request: Restore");
        executor.execute(new Thread(() -> mc.restore(filepath)));
    }

    @Override
    public void delete(String filepath) {
        System.out.println("Request: Delete");
        executor.execute(new Thread(() -> mc.delete(filepath)));
    }

    @Override
    public void reclaim(int disk_space) {
        System.out.println("Request: Reclaim");
        executor.execute(new Thread(() -> mc.reclaim(disk_space)));
    }

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
            e.printStackTrace();
        }
        return null;
    }
}
