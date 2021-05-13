package g24;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemote extends Remote {

    void backup(String filename, int replicationDegree) throws RemoteException;

    void restore(String filename) throws RemoteException;

    void delete(String filename) throws RemoteException;

    String state() throws RemoteException;
}