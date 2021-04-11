package peer;

import channels.*;
import messages.MessageParser;
import storage.*;

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
    private static String version;
    // Channels
    private static MulticastControlChannel controlChannel;
    private static MulticastDataChannel backupChannel;
    private static MulticastDataRecovery restoreChannel;
    // Paths
    private static String serializationPath;
    private static String chunksPath;
    private static String personalFilesPath;
    private static String restoredFilesPath;

    // Protocol
    private static PeerProtocol peerProtocol;
    // Peer stored data
    private static DataStored data = new DataStored();
    // Initializing thread pool executor as a scheduler
    public static ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

    // Macros
    public static final int CHUNK_SIZE = 64000; // Chunk maximum size is 64KB
    public static final String DIRECTORY = "peers/";

    // TCP port
    public static int port;

    public Peer(String version, String peerID, String accessPoint, InetAddress mcAddr, int mcPort, InetAddress mdbAddr, int mdbPort, InetAddress mdrAddr, int mdrPort) throws IOException {
        Peer.version = version;
        Peer.peerID = peerID;

        // Initialize mc channel
        controlChannel = new MulticastControlChannel(mcAddr, mcPort, peerID);
        // Initialize mdb channel
        backupChannel = new MulticastDataChannel(mdrAddr, mdrPort, peerID);
        // Initialize mdr channel
        restoreChannel = new MulticastDataRecovery(mdbAddr, mdbPort, peerID);

        // Start the while loop in the channels, allowing messages to be read
        executor.execute(controlChannel);
        executor.execute(backupChannel);
        executor.execute(restoreChannel);

        // Enable TCP connection for version 2.0
        if(version.equals("2.0")) {
            ServerSocket serverSocket = new ServerSocket(0);
            Peer.port = serverSocket.getLocalPort();
            TCPHandler tcpHandler = new TCPHandler(serverSocket);
            executor.execute(tcpHandler);
        }

        try {
            // Create Remote Object (RMI)
            Peer.peerProtocol = new PeerProtocol(controlChannel, backupChannel, restoreChannel);
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peerProtocol, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);

            System.out.println("Successfully initialized Remote Interface");

        } catch (Exception e) {
            System.err.println("Remote Interface Exception: " + e);
            e.printStackTrace();
        }

        // Creating directories for the peer data
        Peer.chunksPath = DIRECTORY + peerID + "/chunks";
        Peer.personalFilesPath = DIRECTORY + peerID + "/personal_files";
        Peer.restoredFilesPath = DIRECTORY + peerID + "/restored_files";
        serializationPath = DIRECTORY + peerID + "/data.ser";

        createDirectory(DIRECTORY + peerID);
        createDirectory(Peer.chunksPath);
        createDirectory(Peer.personalFilesPath);
        createDirectory(Peer.restoredFilesPath);

        // Reads the serialized data from the peer
        loadChunks();
        // Ensures data is serialized before the application shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::saveChunks));

        // DELETE enhancement sends an HELLO message on peer startup
        if (version.equals("2.0")) {
            // Send HELLO Message
            System.out.println("MC sending :: HELLO Sender " + Peer.getPeerID());
            byte[] message = MessageParser.makeHeader(Peer.getVersion(), "HELLO", Peer.peerID);
            Peer.executor.execute(new Thread(() -> Peer.getMCChannel().sendMessage(message)));
        }
    }

    public static String getPeerID() {
        return peerID;
    }

    public static String getVersion() {
        return version;
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

    public static PeerProtocol getPeerProtocol() {
        return peerProtocol;
    }

    public static String getPersonalFilesPath() {
        return personalFilesPath;
    }

    public static String getChunksPath() {
        return chunksPath;
    }

    public static String getRestoredFilesPath() {
        return restoredFilesPath;
    }

    private void createDirectory(String path) {
        File fileData = new File(path);
        fileData.mkdirs();
    }

    // Chunk serialization, writing in the data.ser file
    public static void saveChunks() {
        try {
            DataStored.createFile(serializationPath);

            FileOutputStream fileOut = new FileOutputStream(serializationPath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(data);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + serializationPath);
        } catch (IOException i) {
            i.printStackTrace();
            System.err.println("Unable to save state in path:" + serializationPath);
        }
    }

    // Chunk serialization, loading the data from a data.ser file
    public static void loadChunks() {
        try {
            File file = new File(serializationPath);
            if(!file.exists())
                return;

            FileInputStream fileIn = new FileInputStream(serializationPath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            data = (DataStored) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.err.println("Class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Deserialized data...");
    }
}