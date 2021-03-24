import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
    void backup(String filepath, int replication_degree) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String filepath) throws RemoteException;
    void reclaim(int disk_space) throws RemoteException;
    String state() throws RemoteException;
}