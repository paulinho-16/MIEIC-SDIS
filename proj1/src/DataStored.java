import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class DataStored implements Serializable {
    private int totalSpace;
    private int occupiedSpace;

    // Armazena-se files ou chunks?????
    private ConcurrentHashMap<String, FileData> personalBackedUpFiles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chunk> backupChunks = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<String> receivedChunks = new CopyOnWriteArraySet<>();

    public DataStored() {
        this.occupiedSpace = 0;
    }

    public boolean hasFileData(String fileID) {
        return personalBackedUpFiles.containsKey(fileID);
    }

    public boolean hasReceivedChunk(String chunkID) {
        return receivedChunks.contains(chunkID);
    }

    public boolean hasChunkBackup(String chunkID) {
        return receivedChunks.contains(chunkID);
    }

    public FileData getFileData(String fileID) {
        return personalBackedUpFiles.get(fileID);
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
        // Verificar se espaço disponível é suficiente para armazenar o ficheiro!!!

        //String key = chunk.getFileID() + "/" + chunk.getChunkNumber();
        // If chunk is already backed up, do nothing
        /*if (backupChunks.containsKey(key)) {
            return;
        }*/
        String key = chunk.getFileID() + "/" + chunk.getChunkNumber();
        if (this.backupChunks.containsKey(key))
            return;

        this.backupChunks.put(key, chunk);

        Peer.getMCChannel().sendStoreMsg(chunk);

        String path = Peer.DIRECTORY + Peer.getPeerID() + "/chunks/" + chunk.getFileID() + "-" + chunk.getChunkNumber();

        // Falta o path
        try{
            File file = createFile(path);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(chunk.getData());
            fout.close();
        }catch(IOException e){
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

    public void addReceivedChunk(String chunkID) {
        receivedChunks.add(chunkID);
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
}
