package storage;

import messages.MessageParser;
import peer.Peer;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Iterator;
import java.util.Map;

public class DataStored implements Serializable {
    // Storage Space
    private int totalSpace;
    private int occupiedSpace;

    //ConcurrentHashMap<fileID,fileData> -> Files that other peers have backed up
    private final ConcurrentHashMap<String, FileData> personalBackedUpFiles = new ConcurrentHashMap<>();
    //ConcurrentHashMap<chunkID,Chunk> -> Chunks the Peer contains as a backup
    private final ConcurrentHashMap<String, Chunk> backupChunks = new ConcurrentHashMap<>();
    //ConcurrentHashMap<chunkID,CopyOnWriteArraySet<PeerID>> -> Register of the list of peers backing up a given chunk
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> chunksRepDegrees = new ConcurrentHashMap<>();
    //CopyOnWriteArraySet<chunkID> -> Chunks not yet received but needed to restore a given file
    private final CopyOnWriteArraySet<String> waitingChunks = new CopyOnWriteArraySet<>();
    //ConcurrentHashMap<ChunkID, Chunk> -> Chunks already received and needed to restore a given file
    private final ConcurrentHashMap<String, Chunk> receivedChunks = new ConcurrentHashMap<>();
    //CopyOnWriteArraySet<ChunkID> -> Chunks that have already been sent, to avoid resending duplicate messages
    private final CopyOnWriteArraySet<String> chunkMessagesSent = new CopyOnWriteArraySet<>();
    //CopyOnWriteArraySet<FileID> -> Files whose deletion has been requested but not all DELETED responses have been received
    private final CopyOnWriteArraySet<String> deletedFiles = new CopyOnWriteArraySet<>();

    public DataStored() {
        this.totalSpace = 500000000; // Default Value: 500 Mb
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

    // Get the current replication degree of a given chunk
    public int getChunkReplicationNum(String chunkID) {
        if (chunksRepDegrees.containsKey(chunkID))
            return chunksRepDegrees.get(chunkID).size();
        return 0;
    }

    // Get the current replication degree of a given file
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

    // If there is no space to store a new chunk, deletes a chunk whose replication degree is above the desired
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
        // Check if the chunk is from a personal file of the current peer, and don't back it up
        if (this.personalBackedUpFiles.containsKey(chunk.getFileID())) {
            String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
            System.out.println("This peer is the owner of the file with the chunk " + chunkID + ". Ignoring PUTCHUNK message.");
            return;
        }

        // Check if Peer already contains the chunk, resending the STORED message as confirmation
        String chunkID = chunk.getFileID() + "-" + chunk.getChunkNumber();
        if (this.backupChunks.containsKey(chunkID)) {
            System.out.println("Chunk has already been stored");
            Peer.getMCChannel().sendStoreMsg(chunk);
            return;
        }

        // Backup enhancement, stopping the storage if the replication degree is already fulfilled
        if(Peer.getVersion().equals("2.0")) {
            int chunkRepDeg = getChunkReplicationNum(chunkID);
            int desiredRepDeg = chunk.getDesiredReplicationDegree();
            if (chunkRepDeg >= desiredRepDeg) {
                System.out.println("Chunk " + chunkID + " already fulfilled repDegree. Ignoring chunk...");
                return;
            }
        }

        // Prevent a Peer from storing empty chunks if its totalSpace is 0
        if (totalSpace == 0) {
            System.out.println("Peer can't store any chunk: total space is 0.");
            return;
        }

        // Check if Peer has enough space to store the chunk, deleting chunks with replication degree above the desired until it has enough space
        if (occupiedSpace + chunk.getSize() > totalSpace) {
            if (!removeExtraChunks(chunk.getSize())) {
                System.out.println("Peer " + Peer.getPeerID() + " doesn't have enough space for chunk " + chunk.getChunkNumber());
                return;
            }
        }

        // Adds the chunk to the storage and updates storage size
        this.backupChunks.put(chunkID, chunk);
        this.occupiedSpace += chunk.getSize();

        // Sends a STORED message through the Multicast Control Channel
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
            System.err.println("Error on writing chunk to a file");
        }
    }

    // Update chunk current replication degree according to the sender of the STORED message
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

    // Delete all chunks associated to a File
    public boolean deleteFileChunks(String fileID) {
        for(String key : backupChunks.keySet()) {
            Chunk chunk = backupChunks.get(key);
            if(chunk.getFileID().equals(fileID)) {
                // Delete the file chunk
                if (!chunk.delete()) {
                    System.err.println("Error deleting chunk file");
                    return false;
                }
                occupiedSpace -= chunk.getSize();
                // Delete the chunk from the storage
                if (backupChunks.remove(key) == null) {
                    System.err.println("Error deleting chunk from backupChunks");
                    return false;
                }
            }
        }

       return true;
    }

    // Check if the peer received all the chunks from the file requested to restore
    public boolean receivedAllChunks(String fileID) {
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
        try {
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + path);
                return myObj;
            } else {
                System.out.println("File already exists.");
            }
        } catch(Exception e) {
            System.err.println("Error on creating file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    // Starts removing random chunks until the occupied space doesn't surpass the total space
    public boolean allocateSpace() {
        // Traversing the backed up chunks using an iterator
        Iterator<Map.Entry<String, Chunk>> itr = backupChunks.entrySet().iterator();

        // Special case of RECLAIM 0, where all chunks should be deleted, regardless of the chunk size
        if(totalSpace == 0) {
            while (itr.hasNext()) {
                if (!deleteChunk(itr))
                    return false;
            }
        }
        else {
            while (spaceExceeded() && itr.hasNext()) {
                if (!deleteChunk(itr))
                    return false;
            }
        }
        return true;
    }

    // Delete a given chunk from the peer storage
    private boolean deleteChunk(Iterator<Map.Entry<String, Chunk>> itr) {
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
        return true;
    }

    // Send a REMOVED message to the MC Channel
    private void sendRemovedMessage(Chunk chunk, String chunkID) {
        Peer.getData().removePeerBackingUpChunk(chunkID, Peer.getPeerID());

        System.out.println("MC sending :: REMOVED " + " file " + chunk.getFileID() + " chunk " + chunk.getChunkNumber() + " Sender " + Peer.getPeerID());
        byte[] message = MessageParser.makeHeader(chunk.getVersion(), "REMOVED", Peer.getPeerID(), chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));
        Peer.executor.execute(new Thread(() -> Peer.getMCChannel().sendMessage(message)));
    }

    // Returns all the peer information as a formatted string
    public String displayState() {
        // For each file whose backup has initiated:
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

    // Removes all peers from the list of peers backing up a given file
    public void resetPeersBackingUp(String fileID) {
        for(String chunkID : chunksRepDegrees.keySet()) {
            String chunkFileID = chunkID.split("-")[0];
            if (chunkFileID.equals(fileID)) {
                chunksRepDegrees.remove(chunkID);
            }
        }
    }

    // Remove a single peer from the list of peers backing up a given file
    public boolean removePeerBackingUp(String fileID, String peerID) {
        boolean deleted = false;
        for(String chunkID : chunksRepDegrees.keySet()) {
            String chunkFileID = chunkID.split("-")[0];
            if (chunkFileID.equals(fileID)) {
                CopyOnWriteArraySet<String> peersBackingUp = chunksRepDegrees.get(chunkID);
                peersBackingUp.remove(peerID);
                deleted = true;
            }
        }
        return deleted;
    }

    // Called on the HELLO message, sends the DELETE messages of the files still waiting to be fully deleted
    public void updateDeletedFiles() {
        for(String fileID : deletedFiles) {
            if(hasFileData(fileID)) {
                String filename = getFileData(fileID).getFilename();
                Peer.getPeerProtocol().delete(filename);
            }
        }
    }
}