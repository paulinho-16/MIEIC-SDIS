import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PeerProtocol implements PeerInterface {
    private final String peerId, version;
    private final MulticastControlChannel mc;
    private final MulticastDataChannel mdb;
    private final MulticastDataRecovery mdr;
    // É suposto usar scheduled thread pool executor
    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

    public PeerProtocol(String version, String peerId, MulticastControlChannel mc, MulticastDataChannel mdb, MulticastDataRecovery mdr) {
        this.version = version;
        this.peerId = peerId;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    @Override
    public void backup(String filepath, int replication_degree) {
        System.out.println("Request: Backup");
        executor.execute(new Thread(() -> {
            try {
                mdb.backup(filepath, replication_degree, version);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void restore(String filepath) {
        executor.execute(new Thread(() -> mc.restore(filepath)));
    }

    @Override
    public void delete(String filepath) {
        executor.execute(new Thread(() -> mc.delete(filepath)));
    }

    @Override
    public void reclaim(int disk_space) {
        executor.execute(new Thread(() -> mdr.reclaim(disk_space)));
    }

    @Override
    public void state() {

    }
}
