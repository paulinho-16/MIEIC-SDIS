package g24.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import g24.Identifier;
import g24.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class Storage {
    
    private ConcurrentHashMap<String, FileData> backupFiles; // Files backed up in the chord network
    private ConcurrentHashMap<String, FileData> storedFiles; // Files stored in this peer file system
	private String path;
    private ScheduledThreadPoolExecutor executor;
    
    public Storage(Identifier id, ScheduledThreadPoolExecutor executor) {
        this.path = "g24/output/peer" + Integer.toString(id.getId());
        this.backupFiles = new ConcurrentHashMap<>();
        this.storedFiles = new ConcurrentHashMap<>();
    }

    // Used by a peer to store a file in non-volatile memory.
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

    // Used by a peer to restore a file.
    public void storeRestored(FileData file) throws IOException {
        String fileDir = this.path + "/restore";
        Files.createDirectories(Paths.get(fileDir));

        Path path = Paths.get(fileDir + "/" + file.getFilename());

        Set<OpenOption> options = new HashSet<OpenOption>();
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.WRITE);

        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, options, this.executor);

        ByteBuffer buffer = ByteBuffer.allocate(file.getData().length);

        buffer.put(file.getData());

        buffer.flip();

        Future<Integer> operation = channel.write(buffer, 0);

        while (!operation.isDone()) {
        }

        channel.close();
    }

    // Used by a peer to read a stored file from memory.
    public FileData read(String fileID) throws IOException, ClassNotFoundException {

        Path path = Paths.get(this.path + "/backup/file-" + fileID + ".ser");

        Set<OpenOption> options = new HashSet<OpenOption>();
        options.add(StandardOpenOption.READ);
        
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, options, this.executor);

        ByteBuffer buffer = ByteBuffer.allocate(Utils.FILE_SIZE * 2);

        Future<Integer> result = channel.read(buffer, 0);

        while (!result.isDone()) {
        }

        buffer.flip();

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
        ObjectInputStream ois = new ObjectInputStream(bais);

        FileData data = (FileData) ois.readObject();

        return data;
    }

    // Verify if a peer has stored a file
    public boolean hasFileStored(String fileID) {
        return this.storedFiles.containsKey(fileID);
    }

    public void addBackupFile(String fileID, FileData fileData) {
        this.backupFiles.put(fileID, fileData);
    }
    
    public FileData getFileData(String fileID){
        // May return null
        return this.storedFiles.get(fileID);
    }

    public FileData getBackupFile(String fileName, int replicationDegree){
        FileData newFileData = new FileData(fileName, replicationDegree);
        String fileID = newFileData.getFileID();
        
        if(this.backupFiles.containsKey(fileID)){
            return this.backupFiles.get(fileID);
        }

        return newFileData;
    }
}
