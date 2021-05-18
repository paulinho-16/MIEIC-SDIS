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
import java.io.File;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Storage {
    
    private ConcurrentHashMap<String, FileData> storedFiles; // Files stored in this peer file system
	private String path;
    private ScheduledThreadPoolExecutor executor;
    private long occupiedSpace = 0;
    private long totalSpace = Utils.MAX_STORAGE;
    
    public Storage(Identifier id, ScheduledThreadPoolExecutor executor) {
        this.path = "g24/output/peer" + Integer.toString(id.getId());
        this.storedFiles = new ConcurrentHashMap<>();
    }

    // Used by a peer to store a file in non-volatile memory.
    public boolean store(FileData file) throws IOException {

        try{
            System.err.println("DATA: " + file.getData().length);
            System.err.println("1: " + file.getSize());
            System.err.println("2: " + this.occupiedSpace);
            System.err.println("3: " + this.totalSpace);
        } catch(Exception e){
            e.printStackTrace();
        }

        if (file.getSize() + this.occupiedSpace > this.totalSpace) {
            return false;
        }

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

        this.occupiedSpace += file.getSize();

        return true;
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
    
    public FileData getFileData(String fileID){
        // May return null
        return this.storedFiles.get(fileID);
    }

    public FileData getBackupFile(String fileName, int replicationDegree){
        FileData newFileData = new FileData(fileName, replicationDegree);
        String fileID = newFileData.getFileID();
        return newFileData;
    }

    public boolean removeFileData(String fileID) {
        try {
            long size = this.storedFiles.get(fileID).getSize();
            this.storedFiles.remove(fileID);
            File file = new File(this.path + "/backup/file-" + fileID + ".ser");
            boolean deleted = Files.deleteIfExists(file.toPath());
            if (deleted) {
                this.occupiedSpace -= size;
            }
            return deleted;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
