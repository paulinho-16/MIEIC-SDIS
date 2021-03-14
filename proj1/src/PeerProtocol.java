import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeerProtocol implements PeerInterface {
    private final String peerId, version;
    private final MulticastControlChannel mc;
    private final MulticastDataRecovery mdb;
    private final MulticastDataChannel mdr;
    // É suposto usar scheduled thread pool executer
    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);


    public PeerProtocol(String version, String peerId, MulticastControlChannel mc, MulticastDataRecovery mdb, MulticastDataChannel mdr){
        this.version = version;
        this.peerId = peerId;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    @Override
    public void backup(String filepath, int replication_degree) {
        executor.execute(new Thread(() -> mdb.backup(filepath,replication_degree)));

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
