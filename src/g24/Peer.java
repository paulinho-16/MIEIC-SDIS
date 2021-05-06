package g24;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import g24.storage.*;
import g24.message.*;

public class Peer implements IRemote {

    private Chord chord;
    private MessageReceiver receiver;
    private Storage storage = new Storage();
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(250);

    // accessPoint ip port [successorIp successorPort]
    public static void main(String[] args) throws Exception {

        // Validate number of arguments
        if (args.length != 3 && args.length != 5) {
            System.err.println("Usage: java Peer <accessPoint> <peerIp> <peerPort> [successorIp successorPort]");
            System.err.println("<accessPoint> -> Access Point of the peer RMI");
            System.err.println("<peerIp> -> IP of the the peer to be initialized");
            System.err.println("<peerPort> -> Port of the the peer to be initialized");

            System.err.println("<successorIp> -> IP address of the successor of the new peer");
            System.err.println("<successorPort> -> Port address of the successor of the new peer");
            return;
        }

        Peer peer;
        Registry registry = null;

        try {
            registry = LocateRegistry.getRegistry();
        } catch (RemoteException e) {
            registry = LocateRegistry.createRegistry(1099);
        } catch (Exception e) {
            Utils.usage(e.getMessage());
            System.exit(1);
        }

        // Parsed Arguments
        String peerAp = args[0];
        String peerIp = args[1];
        int peerPort = Integer.parseInt(args[2]);
        String successorIP;
        int successorPort;

        if(args.length == 5) {
            successorIP = args[3];
            successorPort = Integer.parseInt(args[4]);
            peer = new Peer(peerIp, peerPort, successorIP, successorPort);
        }
        else{
            peer = new Peer(peerIp, peerPort);
        }
        
        IRemote remote = (IRemote) UnicastRemoteObject.exportObject(peer, 0);
        registry.rebind(peerAp, remote);

        System.out.println("REGISTRY :: Peer registered with name " + peerAp);
    }

    public Peer(String ip, int port) {
        try {
            // this.setProperties();
            this.chord = new Chord(ip, port);
            this.receiver = new MessageReceiver(port, this.executor, this.chord);
            this.executor.execute(this.receiver);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.checkPredecessor()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.fix_fingers()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.getSummary()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.stabilize()), 5000, 5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Peer(String ip, int port, String successorIp, int successorPort) {
        try {
            // this.setProperties();
            this.chord = new Chord(ip, port, successorIp, successorPort);
            this.receiver = new MessageReceiver(port, this.executor, this.chord);
            this.executor.execute(this.receiver);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.checkPredecessor()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.fix_fingers()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.getSummary()), 5000, 5000, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.stabilize()), 5000, 5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProperties() {
        // Set the type of the trust store
        System.setProperty("java.net.ssl.trustStoreType", "JKS");
        // Set the password with which the truststore is encripted
        System.setProperty("java.net.ssl.trustStorePassword", "123456");
        // Set the name of the trust store containing the client's public key and certificate
        System.setProperty("java.net.ssl.trustStore", "./keys/truststore");
        // Set the password with which the client keystore is encripted
        System.setProperty("java.net.ssl.keyStorePassword", "123456");
        // Set the name of the keystore containing the server's private and public keys
        System.setProperty("java.net.ssl.keyStore", "./keys/server.keys");
    }

    @Override
    public void backup(String fileName, int replicationDegree) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void restore(String fileName) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(String fileName) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public String state() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
}
