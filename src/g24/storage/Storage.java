package g24.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import g24.Identifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Storage {
    
    private ConcurrentHashMap<String, FileData> backupFiles;
    private ConcurrentHashMap<String, FileData> storedFiles;
	private String path;
    private ScheduledThreadPoolExecutor executor;
    
    public Storage(Identifier id, ScheduledThreadPoolExecutor executor) {
        this.path = "g24/output/peer" + Integer.toString(id.getId());
        this.backupFiles = new ConcurrentHashMap<>();
        this.storedFiles = new ConcurrentHashMap<>();
    }

    /**
     * Used by a peer to store a file in non-volatile memory.
     * @param FileData
     * @throws IOException
     */
    public void store(FileData file) throws IOException {
        String fileDir = this.path + "/backup";
        Files.createDirectories(Paths.get(fileDir));

        Path path = Paths.get(fileDir + "/file-" + file.getFileID() + ".ser");

        Set<OpenOption> options = new HashSet<OpenOption>();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.WRITE);

        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, options, this.executor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(file);
        oos.flush();

        ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
        Future<Integer> operation = channel.write(buffer, 0);
        while (!operation.isDone()) {
        }

        this.storedFiles.put(file.getFileID(), file);

        channel.close();
        oos.close();
        baos.close();
    }

    // public Chunk read(String fileID) throws IOException, ClassNotFoundException {

    //     Path path = Paths.get(this.path + "/backup/file-" + fileId ".ser");

    //     Set<OpenOption> options = new HashSet<OpenOption>();
    //             options.add(StandardOpenOption.READ);
        
    //     AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, options, this.executer);

    //     ByteBuffer buffer = ByteBuffer.allocate(Utils.CHUNK_SIZE * 2);

    //     Future<Integer> result = channel.read(buffer, 0);

    //     while (!result.isDone()) {
    //     }

    //     buffer.flip();

    //     ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
    //     ObjectInputStream ois = new ObjectInputStream(bais);

    //     FileData data = (FileData) ois.readObject();

    //     return data;
    // }

    public boolean hasFileStored(String fileID) {
        return this.storedFiles.containsKey(fileID);
    }

    public void addBackupFile(String fileID, FileData fileData) {
        this.backupFiles.put(fileID, fileData);
    }
    
    // /**
    //  * Used by a peer to check if he was the one who initiated the backup of a file.
    //  * @param fileId
    //  * @return true if the peer was the initiator of a backup for the file, false otherwise
    // */
    // public boolean hasFile(String fileId) {
    //     return this.backupFiles.containsKey(fileId);
    // }
}
