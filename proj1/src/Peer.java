import java.io.*;
import java.net.*;

public class Peer {
    // Constructor parameters
    private String version, peerId, accessPoint;
    // Addresses
    private InetAddress mcAddr, mdbAddr, mdrAddr;
    // Ports
    private int mcPort, mdbPort, mdrPort;
    // Channels
    private Channel controlChannel, backupChannel, restoreChannel;
    // Paths
    private String chunksPath, personalFilesPath, restoredFilesPath;
    // Inicializing thead Pool executor as a scheluded
    static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newScheduledThreadPool(100);

    // Macros
    public static final int CHUNK_SIZE = 64000; // Chunk maximum size i 64KB
    public static final String DIRECTORY = "peers/"; // Temporary value. Directory created for peers in compilation should be added here
    public static final byte CR = 0xD, LF = 0xA;  // ASCII codes for <CRLF>

    public Peer(String version, String peerId, String accessPoint, InetAddress mcAddr, int mcPort, InetAddress mdbAddr, int mdbPort, InetAddress mdrAddr, int mdrPort) throws IOException {
        this.version = version;
        this.peerId = peerId;
        this.accessPoint = accessPoint;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
        this.mdbAddr = mdbAddr;
        this.mdbPort = mdbPort;
        this.mdrAddr = mdrAddr;
        this.mdrPort = mdrPort;

        // Subsribed to all 3 multicast channels for the peer
        // Initialize mc channel
        controlChannel = new Channel(mcAddr, mcPort);
        // Initialize mdb channel
        backupChannel = new Channel(mdbAddr, mdbPort);
        // Initialize mdr channel
        restoreChannel = new Channel(mdrAddr, mdrPort);

        // Using Registry. É assim que se faz para criar um registry quando se têm as funções noutra classe?
        try {
            // Create Remote Object (RMI)
            PeerProtocol obj = new PeerProtocol();
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            // Bind ou rebind? o accessPoint vai ser o mesmo para diferentes 
            registry.bind(accessPoint, stub);

            System.err.println("Successfully initialized Remote Interface");

        } catch (Exception e) {
            System.err.println("Remote Interface Exception: " + e.toString());
            e.printStackTrace();
        }

        // Creating directories for the peer data
        chunksPath = DIRECTORY + peerId + "/chunks";
        personalFilesPath = DIRECTORY + peerId + "/personal_files";
        restoredFilesPath = DIRECTORY + peerId + "/restored_files";

        createDirectory(DIRECTORY + peerId);
        createDirectory(chunksPath);
        createDirectory(personalFilesPath);
        createDirectory(restoredFilesPath);
    }

    private void createDirectory(String path) {
        File file = new File(path);

        //Creating the directory
        if (file.mkdir()) {
            System.out.println("Successfully created directory: " + path);
        } else {
            System.out.println("Failed to create directory with path: " + path);
        }
    }
}