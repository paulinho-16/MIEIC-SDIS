import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Iterator;
import java.util.Map;

public class DataStored implements Serializable {
    private int totalSpace;
    private int occupiedSpace;

    // Armazena-se files ou chunks?????
    private ConcurrentHashMap<String, FileData> personalBackedUpFiles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chunk> backupChunks = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<String> waitingChunks = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<String, Chunk> receivedChunks = new ConcurrentHashMap<>();

    public DataStored() {
        this.totalSpace = Integer.MAX_VALUE;
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

    public void addWaitingChunk(String chunkID) {
        waitingChunks.add(chunkID);
    }

    public void addReceivedChunk(Chunk chunk) {
        receivedChunks.put(chunk.getID(), chunk);
    }

    public void removeWaitingChunk(String chunkID) {
        waitingChunks.remove(chunkID);
    }

    public void removeReceivedChunk(String chunkID) {
        receivedChunks.remove(chunkID);
    }

    public void setTotalSpace(int totalSpace) {
        this.totalSpace = totalSpace;
    }

    // Add a new file to the list of personal backed up files of the peer
    public void addNewFileToMap(FileData fileData) {
        if (!this.personalBackedUpFiles.containsKey(fileData.getFileID())) {
            this.personalBackedUpFiles.put(fileData.getFileID(), fileData);
            System.out.println("ADICIONOU NOVO FILE");
        }
    }

    // Add a new chunk to the list of backed up chunks of a given file
    public void backupNewChunk(Chunk chunk) {
        // TODO:Not adding to backup, need to implement this method
        /*if (!this.backupFiles.containsKey(chunk.getFileID())) {
            return; // File is not in the list of backed up files of the peer
        }
        FileData file = this.backupFiles.get(chunk.getFileID());
        if (!file.hasChunkBackup(chunk.getChunkNumber())) {
            file.addChunkBackup(chunk);
        }*/

    }

    // Return the backed up chunk if exists, return null otherwise
    public Chunk getChunkBackup(String chunkID) {
        /*if (!this.backupFiles.containsKey(fileID))
            return null;

        FileData file = this.backupFiles.get(fileID);

        if (!file.hasChunkBackup(chunkNumber))
            return null;
        return file.getChunkBackup(chunkNumber);*/

        /*for (String key : personalBackedUpFiles.keySet()) {
            FileData file = personalBackedUpFiles.get(key);
            file.getChunkBackup(chunkNumber);

            return null;
        }*/

        return backupChunks.get(chunkID);
    }

    public void storeNewChunk(Chunk chunk) {
        if (occupiedSpace + chunk.getSize() > totalSpace) {
            System.out.println("Peer " + Peer.getPeerID() + " doesn't have enough space for chunk " + chunk.getChunkNumber());
            return;
        }

        String key = chunk.getFileID() + "-" + chunk.getChunkNumber();
        if (this.backupChunks.containsKey(key))
            return;

        this.backupChunks.put(key, chunk);
        this.occupiedSpace += chunk.getSize();

        Peer.getMCChannel().sendStoreMsg(chunk);

        String path = Peer.DIRECTORY + Peer.getPeerID() + "/chunks/" + chunk.getFileID() + "-" + chunk.getChunkNumber();

        // Falta o path
        try {
            File file = createFile(path);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(chunk.getData());
            fout.close();
        } catch(IOException e){
            System.out.println("Error on writing chunk to a file");
        }
    }

    public void updateChunkReplicationsNum(String fileID, int chunkNumber, String senderID) {
        // If the sender of the message is not registered as a Peer backing the chunk, register it
        /*if (this.backupFiles.containsKey(fileID)) {
            FileData file = this.backupFiles.get(fileID);
            if (file.hasChunkBackup(chunkNo)) {
                Chunk chunk = file.getChunkBackup(chunkNo);
                if (!chunk.isPeerBackingUp(senderID)) {
                    chunk.addPeerBackingUp(senderID);
                }
            }
        }
        else {     // If the file is not contained
            //this.backupFiles.put(fileID, new FileData());
        }*/

        if (!this.personalBackedUpFiles.containsKey(fileID))
            return;

        FileData file = this.personalBackedUpFiles.get(fileID);
        file.addPeerBackingUp(chunkNumber, senderID);
    }

    public void deleteFileFromMap(String fileID) {
        personalBackedUpFiles.remove(fileID);
    }

    public boolean deleteFileChunks(String fileID) {
        // Delete all files from all chunks
        System.out.println("Entered deleteFileChunks");
        for(String key : backupChunks.keySet()) {
            Chunk chunk = backupChunks.get(key);
            if(chunk.getFileID().equals(fileID)) {
                // Delete the file chunk
                if (!chunk.delete()) {
                    System.out.println("Error deleting chunk file");
                    return false;
                }
                occupiedSpace -= chunk.getSize();

                if (backupChunks.remove(key) == null){
                    System.out.println("Error deleting chunk from backupChunks");
                    return false;
                }
            }
        }

        //System.out.println("BACKUPFILE ANTES " + backupFiles.size());
        /*for (String key : back.keySet()) {
            ConcurrentHashMap<Integer, Chunk> chunks = backupFiles.get(key).getBackupChunks();
            for (Integer id : chunks.keySet()) {
                if(!chunks.get(id).delete()){
                    System.out.println("Error on deleting chunk " + id);
                    return false;
                }
            }
        }*/

        //System.out.println("BACKUPFILE DEPOIS " + backupFiles.size());
        // Delete the chunk file from the backupFiles Map
        /*if (this.backupFiles.remove(fileID) == null) {
            System.out.println("Error deleting file from map");
            return false;
        }*/

       return true;
    }

    public boolean spaceExceeded() {
        System.out.println("TotalSpace: " + totalSpace);
        System.out.println("OccupiedSpace: " + occupiedSpace);
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
                // Verificar se ta remoção é feita com sucesso?
                backupChunks.remove(chunk.getID());
                occupiedSpace -= spaceFreed;

                // "1.0", "REMOVED", peerID , fileID, Integer.toString(chunkNumber)
                // Version?? Associar a quê?
                byte[] message = MessageParser.makeHeader("1.0", "REMOVED", Peer.getPeerID(), chunk.getFileID(), Integer.toString(chunk.getChunkNumber()));
                Peer.getMCChannel().threads.execute(new Thread(() -> Peer.getMCChannel().sendMessage(message)));
            }
            else
                return false;
        }
        return true;
    }
}
