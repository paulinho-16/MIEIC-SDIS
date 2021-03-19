import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStored implements Serializable {
    private int totalSpace;
    private int occupiedSpace;

    // Armazena-se files ou chunks?????
    private ConcurrentHashMap<String, FileData> backupFiles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chunk> backupChunks = new ConcurrentHashMap<>();

    public DataStored() {
        this.occupiedSpace = 0;
    }

    // Add a new file to the list of backed up files of the peer
    public void backupNewFile(FileData fileData) {
        if (!this.backupFiles.containsKey(fileData.getFileID())) {
            this.backupFiles.put(fileData.getFileID(), fileData);
        }
    }

    // Add a new chunk to the list of backed up chunks of a given file
    public void backupNewChunk(Chunk chunk) {
        if (!this.backupFiles.containsKey(chunk.getFileID())) {
            return; // File is not in the list of backed up files of the peer
        }
        FileData file = this.backupFiles.get(chunk.getFileID());
        if (!file.hasChunkBackup(chunk.getChunkNumber())) {
            file.addChunkBackup(chunk);
        }
    }

    // Return the backed up chunk if exists, return null otherwise
    public Chunk getBackupChunk(String fileID, int chunkNumber) {

        if (!this.backupFiles.containsKey(fileID))
            return null;

        FileData file = this.backupFiles.get(fileID);

        if (!file.hasChunkBackup(chunkNumber))
            return null;
        return file.getChunkBackup(chunkNumber);
    }

    public void storeNewChunk(Chunk chunk) {
        // Verificar se espaço disponível é suficiente para armazenar o ficheiro!!!

        //String key = chunk.getFileID() + "/" + chunk.getChunkNumber();
        // If chunk is already backed up, do nothing
        /*if (backupChunks.containsKey(key)) {
            return;
        }*/
        if (this.backupFiles.containsKey(chunk.getFileID())) {
            FileData file = this.backupFiles.get(chunk.getFileID());
            if (file.hasChunkBackup(chunk.getChunkNumber()))
                return;
        }

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

    public void updateChunkReplicationsNum(String fileID, int chunkNo, String senderID) {
        // If the sender of the message is not registered as a Peer backing the chunk, register it
        if (this.backupFiles.containsKey(fileID)) {
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
        }
    }

    public static File createFile(String path) {
        try{
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                //System.out.println("File created: " + path);
                return myObj;
            } else {
                //System.out.println("File already exists.");
            }
        }catch(Exception e){
            System.out.println("Error on creating file: " + path);
            e.printStackTrace();
        }
        return null;
    }


}
