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
    private static String serializationPath;
    // Protocol
    private PeerProtocol peerProtocol;
    // Peer stored data
    private static DataStored data = new DataStored();
    // Inicializing thead Pool executor as a scheduled
    static ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(200);

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
        serializationPath = "serialization/peer" + peerID + ".ser";

        createDirectory(DIRECTORY + peerID);
        createDirectory(chunksPath);
        createDirectory(personalFilesPath);
        createDirectory(restoredFilesPath);
        createDirectory("serialization");

        // Reads the serialized data from the peer
        loadChunks();
        // Ensures data is serialized before the application shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::saveChunks));
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

    public static MulticastDataRecovery getMDRChannel() {
        return restoreChannel;
    }

    private void createDirectory(String path) {
        File fileData = new File(path);
        fileData.mkdirs();
    }

    // Synchronized? Vários gits têm isto, o que faz sentido tendo em conta o uso de threads (verificar mais tarde)
    public static void saveChunks() {
        try {
            DataStored.createFile(serializationPath);
            // Este path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileOutputStream fileOut = new FileOutputStream(serializationPath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            // Deve-se escrever todos os objetos que se deseja, neste caso ainda só temos um chunkMap
            out.writeObject(data);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + serializationPath);
        } catch (IOException i) {
            i.printStackTrace();
            System.out.println("Unable to save state in path:" + serializationPath);
        }
        System.out.println("Serialized data");
    }

    public static void loadChunks(){
        try {
            File file = new File(serializationPath);
            if(!file.exists())
                return;
            // Este path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileInputStream fileIn = new FileInputStream(serializationPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            // Se depois houver mais estruturas de dados, é preciso lê-las na mesma ordem que se escrevem
            data = (DataStored) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Deserialized data...");
    }
}