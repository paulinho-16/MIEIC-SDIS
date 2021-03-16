import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Peer {
    // Constructor parameters
    private static String peerID;
    private String version, accessPoint;
    // Addresses
    private InetAddress mcAddr, mdbAddr, mdrAddr;
    // Ports
    private int mcPort, mdbPort, mdrPort;
    // Channels
    private static MulticastControlChannel controlChannel;
    private static MulticastDataChannel backupChannel;
    private static MulticastDataRecovery restoreChannel;
    // Paths
    private String chunksPath, personalFilesPath, restoredFilesPath;
    // Protocol
    private PeerProtocol peerProtocol;
    // Peer stored data
    private static DataStored data = new DataStored();
    // Inicializing thead Pool executor as a scheduled
    static ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(100);

    // Macros
    public static final int CHUNK_SIZE = 64000; // Chunk maximum size i 64KB
    public static final String DIRECTORY = "peers/"; // Temporary value. Directory created for peers in compilation should be added here
    public static final byte CR = 0xD, LF = 0xA;  // ASCII codes for <CRLF>

    public Peer(String version, String peerID, String accessPoint, InetAddress mcAddr, int mcPort, InetAddress mdbAddr, int mdbPort, InetAddress mdrAddr, int mdrPort) throws IOException {
        this.version = version;
        this.peerID = peerID;
        this.accessPoint = accessPoint;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mdrAddr = mdrAddr;
        this.mdrPort = mdrPort;

        // Subscribed to all 3 multicast channels for the peer
        // Initialize mc channel
        controlChannel = new MulticastControlChannel(mcAddr, mcPort, peerID);
        // Initialize mdb channel
        backupChannel = new MulticastDataChannel(mdrAddr, mdrPort, peerID);
        // Initialize mdr channel
        restoreChannel = new MulticastDataRecovery(mdbAddr, mdbPort, peerID);

        executor.execute(controlChannel);
        executor.execute(backupChannel);
        executor.execute(restoreChannel);

        // Using Registry. É assim que se faz para criar um registry quando se têm as funções noutra classe?
        try {
            // Create Remote Object (RMI)
            this.peerProtocol = new PeerProtocol(version,peerID,controlChannel, backupChannel, restoreChannel);
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peerProtocol, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            // Bind ou rebind? o accessPoint vai ser o mesmo para diferentes
            registry.rebind(accessPoint, stub);

            System.err.println("Successfully initialized Remote Interface");

        } catch (Exception e) {
            System.err.println("Remote Interface Exception: " + e.toString());
            e.printStackTrace();
        }

        // Creating directories for the peer data
        chunksPath = DIRECTORY + peerID + "/chunks";
        personalFilesPath = DIRECTORY + peerID + "/personal_files";
        restoredFilesPath = DIRECTORY + peerID + "/restored_files";

        createDirectory(DIRECTORY + peerID);
        createDirectory(chunksPath);
        createDirectory(personalFilesPath);
        createDirectory(restoredFilesPath);
    }

    public static String getPeerID() {
        return peerID;
    }

    public PeerProtocol getProtocol() {
        return peerProtocol;
    }

    public static DataStored getData() {
        return data;
    }

    public static MulticastControlChannel getMCChannel() {
        return controlChannel;
    }

    public static MulticastDataChannel getMDBChannel() {
        return backupChannel;
    }


    private void createDirectory(String path) {
        File fileData = new File(path);

        //Creating the directory
        if (fileData.mkdirs()) {
            System.out.println("Successfully created directory: " + path);
        } else {
            System.out.println("Failed to create directory with path: " + path);
        }
    }
}