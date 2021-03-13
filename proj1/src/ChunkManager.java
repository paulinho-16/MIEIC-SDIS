import java.io.*
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager{

    private final int serverId;
    private final String path;
    private ConcurrentHashMap<String,Chunk> chunkMap = new ConcurrentHashMap<>();

    // Colocar o path como uma veriavel constante?
    public ChunkManager(int serverId, String path){
        this.serverId = serverId;
        this.path = path;
    }

    // Synchronized? Vários gits têm isto, o que faz sentido tendo em conta o uso de threads (verificar mais tarde)
    private synchronized void saveChunks() {
        try {
            // ESte path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            // Deve-se escrever todos os objetos que se deseja, neste caso ainda só temos um chunkMap
            out.writeObject(chunkMap);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + path);
        } catch (IOException i) {
            i.printStackTrace();
            System.out.println("Unable to save state in path:" + path);
        }
        System.out.println("Serialized chunkMap");
    }

    private void loadChunks(){
        try {
            // ESte path ainda não está bem, é preciso atribuir um diretório correto mais tarde
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            // Se depois houver mais estruturas de dados, é preciso lê-las na mesma ordem que se escrevem
            chunkMap = (ConcurrentHashMap<String, Integer>) in.readObject();
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