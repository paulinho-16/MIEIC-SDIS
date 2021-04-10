import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Iterator;
import java.util.Map;

public class DataStored implements Serializable {
    private int totalSpace;
    private int occupiedSpace;

    private final ConcurrentHashMap<String, FileData> personalBackedUpFiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Chunk> backupChunks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> chunksRepDegrees = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<String> waitingChunks = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<String, Chunk> receivedChunks = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<String> chunkMessagesSent = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<String> deletedFiles = new CopyOnWriteArraySet<>();

    public DataStored() {
        this.totalSpace = 500000000; // Default Value: 500MB
        this.occupiedSpace = 0;
    }

    public boolean hasFileData(String fileID) {
        return personalBackedUpFiles.containsKey(fileID);
    }

    public boolean hasChunkBackup(String chunkID) {
        return backupChunks.containsKey(chunkID);
    }

    public boolean hasWaitingChunk(String chunkID) {
        return waitingChunks.contains(chunkID);
    }

    public boolean hasReceivedChunk(String chunkID) {
        return receivedChunks.containsKey(chunkID);
    }

    public boolean hasChunkMessagesSent(String chunkID) {return chunkMessagesSent.contains(chunkID);}

    public FileData getFileData(String fileID) {
        return personalBackedUpFiles.get(fileID);
    }

    public Chunk getReceivedChunk(String chunkID) {
        return receivedChunks.get(chunkID);
    }

    public int getTotalSpace() {
        return totalSpace;
    }

    public int getOccupiedSpace() {
        return occupiedSpace;
    }

    public int getChunkReplicationNum(String chunkID) {
        if (chunksRepDegrees.containsKey(chunkID))
            return chunksRepDegrees.get(chunkID).size();
        return 0;
    }

    public int getFileReplicationDegree(String fileID) {
        FileData fileData = personalBackedUpFiles.get(fileID);
        CopyOnWriteArraySet<String> chunks = fileData.getBackupChunks();
        int curRepDeg = Integer.MAX_VALUE;

        for (String chunkID : chunks) {
            if (!chunksRepDegrees.containsKey(chunkID))
                return 0;
            int chunkRepDeg = chunksRepDegrees.get(chunkID).size();
            if (curRepDeg > chunkRepDeg) {
                curRepDeg = chunkRepDeg;
            }
        }

        return curRepDeg;
    }

    public void addWaitingChunk(String chunkID) {
        waitingChunks.add(chunkID);
    }

    public void addReceivedChunk(Chunk chunk) {
        receivedChunks.put(chunk.getID(), chunk);
    }

    public void addChunkMessagesSent(String chunkID) {chunkMessagesSent.add(chunkID);}

    public void addDeletedFile(String fileID) { deletedFiles.add(fileID);}

    public void removeDeletedFile(String fileID) { deletedFiles.remove(fileID);}

    public void removeWaitingChunk(String chunkID) {
        waitingChunks.remove(chunkID);
    }

    public void removeReceivedChunk(String chunkID) {
        receivedChunks.remove(chunkID);
    }

    public void removeChunkMessagesSent(String chunkID) {chunkMessagesSent.remove(chunkID);}

    public void removePeerBackingUpChunk(String chunkID, String peerID) {
        if (chunksRepDegrees.containsKey(chunkID)){
            chunksRepDegrees.get(chunkID).remove(peerID);
        }
    }

    public void setTotalSpace(int totalSpace) {
        this.totalSpace = totalSpace;
    }

    // Add a new file to the list of personal backed up files of the peer
    public void addNewFileToMap(FileData fileData) {
        if (!this.personalBackedUpFiles.containsKey(fileData.getFileID())) {
            this.personalBackedUpFiles.put(fileData.getFileID(), fileData);
        }
    }

    public boolean removeExtraChunks(int chunkSize) {
        for (String chunkID : backupChunks.keySet()) {
            Chunk storedChunk = backupChunks.get(chunkID);
            // Delete backed up chunk with replication degree greater than the desired
            int chunkRepDeg = getChunkReplicationNum(chunkID);
            if (chunkRepDeg > storedChunk.getDesiredReplicationDegree()) {
                if (storedChunk.delete()) {
                    backupChunks.remove(chunkID);
                    occupiedSpace -= storedChunk.getSize();
                    sendRemovedMessage(storedChunk, chunkID);
                }
                if (occupiedSpace + chunkSize <= totalSpace) {
                    return true;
                }
            }
        }
        return false;
    }

    // Return the backed up chunk if exists, return null otherwise
    public Chunk getChunkBackup(String chunkID) {
        return backupChunks.get(chunkID);
    }

    public void storeNewChunk(Chunk chunk) {
        if (this.personalBackedUpFiles.containsKey(chunk.getFileID())) {
            String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
            System.out.println("This peer is the owner of the file with the chunk " + chunkID + ". Ignoring PUTCHUNK message.");
            return;
        }

        // Check if Peer already contains the chunk
        String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
        if (this.backupChunks.containsKey(chunkID)) {
            System.out.println("Chunk has already been stored");
            Peer.getMCChannel().sendStoreMsg(chunk);
            return;
        }

        // Backup enhancement
        if(Peer.getVersion().equals("2.0")) {
            int chunkRepDeg = getChunkReplicationNum(chunkID);
            int desiredRepDeg = chunk.getDesiredReplicationDegree();
            if (chunkRepDeg >= desiredRepDeg) {
                System.out.println("Chunk " + chunkID + " already fulfilled repDegree. Ignoring chunk...");
                return;
            }
        }

        // Check if Peer has enough space to store the chunk
        if (occupiedSpace + chunk.getSize() > totalSpace) {
            if (!removeExtraChunks(chunk.getSize())) {
                System.out.println("Peer " + Peer.getPeerID() + " doesn't have enough space for chunk " + chunk.getChunkNumber());
                return;
            }
        }

        this.backupChunks.put(chunkID, chunk);
        this.occupiedSpace += chunk.getSize();

        Peer.getMCChannel().sendStoreMsg(chunk);

        // Writing the Chunk to a file
        try {
            String path = Peer.getChunksPath() + "/" + chunk.getFileID() + "-" + chunk.getChunkNumber();
            File file = createFile(path);
            assert file != null: "Chunk file hasn't been created";
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(chunk.getData());
            fout.close();
        } catch(IOException e) {
            System.out.println("Error on writing chunk to a file");
        }
    }

    public void updateChunkReplicationsNum(String fileID, int chunkNumber, String senderID) {
        String chunkID = fileID + "-" + chunkNumber;
        CopyOnWriteArraySet<String> peersStoring = chunksRepDegrees.get(chunkID);
        if (peersStoring == null) {
            peersStoring = new CopyOnWriteArraySet<>();
            peersStoring.add(senderID);
            chunksRepDegrees.put(chunkID, peersStoring);
        }
        else {
            peersStoring.add(senderID);
        }
    }

    public void deleteFileFromMap(String fileID) {
        personalBackedUpFiles.remove(fileID);
    }

    public boolean deleteFileChunks(String fileID) {
        // Delete all chunks of the file with fileID
        for(String key : backupChunks.keySet()) {
            Chunk chunk = backupChunks.get(key);
            if(chunk.getFileID().equals(fileID)) {
                // Delete the file chunk
                if (!chunk.delete()) {
                    System.out.println("Error deleting chunk file");
                    return false;
                }
                occupiedSpace -= chunk.getSize();
                // Delete the chunk from the storage
                if (backupChunks.remove(key) == null){
                    System.out.println("Error deleting chunk from backupChunks");
                    return false;
                }
            }
        }

       return true;
    }

    boolean receivedAllChunks(String fileID) {
        FileData fileData = personalBackedUpFiles.get(fileID);
        CopyOnWriteArraySet<String> chunks = fileData.getBackupChunks();
        for (String chunkID : chunks) {
            if (!receivedChunks.containsKey(chunkID)) {
                return false;
            }
        }
        return true;
    }

    public boolean spaceExceeded() {
        return occupiedSpace > totalSpace;
    }

    public static File createFile(String path) {
        try{
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + path);
                return myObj;
            } else {
                System.out.println("File already exists.");
            }
        }catch(Exception e){
            System.out.println("Error on creating file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    public boolean allocateSpace() {
        // Traversing the backed up chunks using an iterator
        Iterator<Map.Entry<String, Chunk>> itr = backupChunks.entrySet().iterator();

        while (spaceExceeded() && itr.hasNext()) {
            Chunk chunk = itr.next().getValue();
            int spaceFreed = chunk.getSize();
            if (chunk.delete()) {
                backupChunks.remove(chunk.getID());
                occupiedSpace -= spaceFreed;

                String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
                sendRemovedMessage(chunk, chunkID);
            }
            else
                return false;
        }
        return true;
    }

    private void sendRemovedMessage(Chunk chunk, String chunkID) {
        Peer.getData().removePeerBackingUpChunk(chunkID, Peer.getPeerID());

        System.out.println("MC sending :: REMOVED " + " file " + chunk.getFileID() + " chunk " + chunk.getChunkNumber() + " Sender " + Peer.getPeerID());
        byte[] message = MessageParser.makeHeader(chunk.getVersion(), "REMOVED", Peer.getPeerID(), chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));
        Peer.executor.execute(new Thread(() -> Peer.getMCChannel().sendMessage(message)));
    }

    public String displayState() {
        // For each file whose backup it has initiated:
        StringBuilder builder = new StringBuilder();
        builder.append("\nPersonal backed up files:");
        if (personalBackedUpFiles.isEmpty()) {
            builder.append(" None");
        }
        else {
            for (String key : personalBackedUpFiles.keySet()) {
                FileData fileData = personalBackedUpFiles.get(key);
                builder.append("\n\tPathname: ").append(Peer.getPersonalFilesPath()).append("/").append(fileData.getFilename());
                builder.append("\n\tFileID: ").append(fileData.getFileID());
                int desiredRepDegree = fileData.getReplicationDegree();
                builder.append("\n\tDesired Replication Degree: ").append(desiredRepDegree);
                builder.append("\n\tFile Chunks:");

                for (String chunkID : fileData.getBackupChunks()) {
                    int perceivedReplicationDegree = getChunkReplicationNum(chunkID);
                    builder.append("\n\t\t ChunkID: ").append(chunkID); // Meter chunkNumber em vez de chunkID???
                    builder.append("\n\t\t Perceived Replication Degree: ").append(perceivedReplicationDegree);
                }
            }
        }

        builder.append("\nStored Chunks:");
        if (backupChunks.isEmpty()) {
            builder.append(" None");
        }
        else {
            for (String key : backupChunks.keySet()) {
                Chunk chunk = backupChunks.get(key);
                String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
                int perceivedRepDegree = getChunkReplicationNum(chunkID);
                builder.append("\n\tChunkID: ").append(chunkID);
                builder.append("\n\tSize: ").append(chunk.getSize()).append(" bytes");
                builder.append("\n\tDesired RepDegree: ").append(chunk.getDesiredReplicationDegree());
                builder.append("\n\tPerceived RepDegree: ").append(perceivedRepDegree);
            }
        }

        builder.append("\nStorage Capacity:");
        int freeSpace = totalSpace - occupiedSpace;
        builder.append("\n\tFree Space: ").append(freeSpace).append(" bytes");
        builder.append("\n\tOccupied Space ").append(occupiedSpace).append(" bytes");

        return builder.toString();
    }

    public void resetPeersBackingUp(String fileID) {
        for(String chunkID : chunksRepDegrees.keySet()) {
            String chunkFileID = chunkID.split("-")[0];
            if (chunkFileID.equals(fileID)) {
                chunksRepDegrees.remove(chunkID);
            }
        }
    }

    public void removePeerBackingUp(String fileID, String peerID) {
        for(String chunkID : chunksRepDegrees.keySet()) {
            String chunkFileID = chunkID.split("-")[0];
            if (chunkFileID.equals(fileID)) {
                CopyOnWriteArraySet<String> peersBackingUp = chunksRepDegrees.get(chunkID);
                peersBackingUp.remove(peerID);
            }
        }
    }

    public void updateDeletedFiles() {
        for(String fileID : deletedFiles) {
            if(hasFileData(fileID)) {
                String filename = getFileData(fileID).getFilename();
                Peer.getPeerProtocol().delete(filename);
            }
        }
    }
}