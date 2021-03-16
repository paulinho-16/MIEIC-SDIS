import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileData implements Serializable {
    private String path;
    private String fileID;
    private int replicationDegree;

    private ConcurrentHashMap<Integer, Chunk> backupChunks = new ConcurrentHashMap<>();

    public FileData(String path, String fileID, int replicationDegree) {
        this.path = path;
        this.fileID = fileID;
        this.replicationDegree = replicationDegree;
    }

    public String getPath() {
        return path;
    }

    public String getFileID() {
        return fileID;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public boolean hasChunkBackup(int chunkNumber) {
        return backupChunks.containsKey(chunkNumber);
    }

    public void addChunkBackup(Chunk chunk) {
        this.backupChunks.put(chunk.getChunkNumber(), chunk);
    }

    public Chunk getChunkBackup(int chunkNumber) {
        return this.backupChunks.get(chunkNumber);
    }

    private synchronized void saveChunks() {
        try {
            // Este path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            // Deve-se escrever todos os objetos que se deseja, neste caso ainda só temos um chunkMap
            out.writeObject(backupChunks);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in " + path);
        } catch (IOException i) {
            i.printStackTrace();
            System.out.println("Unable to save state in path:" + path);
        }
        System.out.println("Serialized chunkMap");
    }

    private void loadChunks() {
        try {
            // ESte path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            // Se depois houver mais estruturas de dados, é preciso lê-las na mesma ordem que se escrevem
            backupChunks = (ConcurrentHashMap<Integer, Chunk>) in.readObject();
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
        System.out.println("Deserialized chunkMap...");
    }
}