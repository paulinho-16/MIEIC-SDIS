package g24;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import g24.storage.*;
import g24.handler.BackupHandler;
import g24.handler.BackupExtraHandler;
import g24.message.*;

public class Peer implements IRemote {

    private Chord chord;
    private MessageReceiver receiver;
    private Storage storage;
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
            this.chord = new Chord(ip, port);
            this.storage = new Storage(this.chord.getId(), this.executor);
            this.receiver = new MessageReceiver(port, this.executor, this.chord, this.storage);
            this.executor.execute(this.receiver);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.checkPredecessor()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.fix_fingers()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.getSummary()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.stabilize()), 1000, 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Peer(String ip, int port, String successorIp, int successorPort) {
        try {
            this.chord = new Chord(ip, port, successorIp, successorPort);
            this.storage = new Storage(this.chord.getId(), this.executor);
            this.receiver = new MessageReceiver(port, this.executor, this.chord, this.storage);
            this.executor.execute(this.receiver);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.checkPredecessor()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.fix_fingers()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.getSummary()), 1000, 500, TimeUnit.MILLISECONDS);
            this.executor.scheduleWithFixedDelay(new Thread(() -> this.chord.stabilize()), 1000, 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backup(String fileName, int replicationDegree) throws RemoteException {
        
        try {
            FileData fileData = new FileData(fileName, replicationDegree);
            int counter = Math.min(replicationDegree, Utils.m);
            this.storage.addBackupFile(fileData.getFileID(), fileData);

            if(replicationDegree >= Math.pow(2, Utils.m)){
                System.out.println("The replication degree that was requested cannot be fulfilled.");
                return;
            }
            
            System.out.println(this.chord.getFingerTable().values().toString());
            HashSet<Identifier> fingers = new HashSet<Identifier>();
            for(Identifier finger : this.chord.getFingerTable().values())
                fingers.add(finger);
            System.out.println(fingers.toString());

            // Send the BACKUP message to the peers in the finger table
            for(Identifier receiver : fingers) {
                
                if(counter == 0)
                    break;

                if(!this.chord.getId().equals(receiver)) {
                    System.out.println("SEND: " + receiver.toString());
                    this.executor.execute(new BackupHandler(this.chord, receiver, fileData));
                    counter--;
                }
            }

            // If the replication degree is not fulfilled, ask the peers in the finger table to execute the backup protocol
            this.executor.schedule( new Thread(()-> {
                
                ConcurrentHashMap.KeySetView<Identifier,Boolean> set = fileData.getPeers().keySet();
                int repLeft = replicationDegree - fileData.getTotalPeers();
                
                for (Identifier sender : set) {
                    if (repLeft == 0)
                        break;
                    int repDeg = repLeft < Utils.m ? repLeft : Utils.m;
                    if(!this.chord.getId().equals(sender)) {
                        this.executor.execute(new BackupExtraHandler( sender, fileData, repDeg, this.chord));
                    }
                    repLeft -= repDeg;
                }
            }), 2000, TimeUnit.MILLISECONDS);
        
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Could not backup file " + fileName);
        }
    }

    @Override
    public void restore(String fileName) throws RemoteException {

        String fileID = "";

        try {
            fileID = Utils.generateFileHash(fileName);
        
            if(this.storage.hasFileStored(fileID)) {
                
                FileData fileData = this.storage.read(fileID);
                this.storage.storeRestored(fileData);
            }
            else {
                // Request the backup from other peers
                System.out.println("File: " + fileName + "doesn't exist");
            }
        } catch (ClassNotFoundException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.out.println("File: " + fileName + "could not be stored");
        }
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
