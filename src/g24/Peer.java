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
import g24.storage.*;
import g24.handler.BackupHandler;
import g24.handler.RestoreHandler;
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
    public void backup(String filename, int replicationDegree) throws RemoteException {
        
        try {
            FileData fileData = this.storage.getBackupFile(filename, replicationDegree);

            this.storage.addBackupFile(fileData.getFileID(), fileData);
            
            Identifier fileKey = new Identifier(Utils.generateHash(fileData.getFilename()));
            
            // send to backupNode
            Identifier backupNode = this.chord.findSuccessor(fileKey);
            //System.out.println("BACKUPNODE: " + backupNode.toString());

            HashSet<Identifier> nextPeers = new HashSet<Identifier>();
            nextPeers.add(backupNode);

            int i = 1;

            while ((nextPeers.size() < replicationDegree) && (Utils.m >= i)) {

                Identifier successor = this.chord.findSuccessor(backupNode.getNext(i));
                // System.out.println("SUCCESSOR: " + successor.toString());
                nextPeers.add(successor);
                i++;
            }

            for(Identifier peer : nextPeers) {

                if(peer.equals(this.chord.getId())) {                    
                    this.storage.store(new FileData(fileData.getFileID(), fileData.getData()));
                    fileData.addPeer(this.chord.getId());
                }
                else {
                    this.executor.execute(new BackupHandler(this.chord, peer, fileData));
                }
            }

            this.executor.schedule( new Thread(()-> {
                System.out.println("Replication degree: " + fileData.getTotalPeers() + " out of " + replicationDegree + " desired copies");
            }), 2000, TimeUnit.MILLISECONDS);
            
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Could not backup file " + filename);
        }
    }

    @Override
    public void restore(String filename) throws RemoteException {

        String fileID = "";

        try {
            fileID = Utils.generateFileHash(filename);
        
            // If the peer has the file in its storage
            if(this.storage.hasFileStored(fileID)) {
                
                FileData fileData = this.storage.read(fileID);
                fileData.setFilename(filename);
                this.storage.storeRestored(fileData);
            }
            else {
                // Request the backup from other peers
                Identifier fileKey = new Identifier(Utils.generateHash(filename));
                Identifier backupNode = this.chord.findSuccessor(fileKey);

                HashSet<Identifier> nextPeers = new HashSet<Identifier>();
                nextPeers.add(backupNode);

                int i = 1;

                while (Utils.m >= i) {
                    Identifier successor = this.chord.findSuccessor(backupNode.getNext(i));
                    nextPeers.add(successor);
                    i++;
                }

                this.executor.execute(new RestoreHandler(this.chord, nextPeers, new FileData(fileID, filename), this.storage));
            }
        } catch (ClassNotFoundException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            System.out.println("File: " + filename + "could not be stored");
        }
    }

    @Override
    public void delete(String filename) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public String state() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }
}
