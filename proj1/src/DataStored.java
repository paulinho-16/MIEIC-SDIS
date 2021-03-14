import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class DataStored implements Serializable {
    private int totalSpace;
    private int occupiedSpace;

    // Armazena-se files ou chunks?????
    private final ConcurrentHashMap<String, FileData> backupFiles = new ConcurrentHashMap<>();

    public DataStored() {
        this.occupiedSpace = 0;
    }

    // Add a new file to the list of backed up files of the peer
    public void backupNewFile(FileData fileData) {
        if (!this.backupFiles.containsKey(fileData.getFileID()))
            this.backupFiles.put(fileData.getFileID(), fileData);
    }

    // Add a new chunk to the list of backed up chunks of a given file
    public void backupNewChunk(Chunk chunk) {
        if (!this.backupFiles.containsKey(chunk.getFileID()))
            return; // File is not in the list of backed up files of the peer
        FileData file = this.backupFiles.get(chunk.getFileID());
        if (!file.hasChunkBackup(chunk.getChunkNumber()))
            file.addChunkBackup(chunk);
    }

    // Return the backed up chunk if exists, return null otherwise
    public Chunk getBackupChunk(String fileID, int chunkNumber) {
        if (this.backupFiles.containsKey(fileID))
            return null;
        FileData file = this.backupFiles.get(fileID);
        if (file.hasChunkBackup(chunkNumber))
            return null;
        return file.getChunkBackup(chunkNumber);
    }

    public void storeNewChunk(Chunk chunk) {

    }
}
