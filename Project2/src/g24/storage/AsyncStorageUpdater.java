package g24.storage;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * Serializes the storage and saves it in non-volatile memory
*/
public class AsyncStorageUpdater implements Runnable {
    private Storage storage;

    public AsyncStorageUpdater(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(Paths.get(this.storage.getPath()));

            Path path = Paths.get(this.storage.getPath() + "/storage.ser");

            AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this.storage);
            oos.flush();

            ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
            Future<Integer> operation = channel.write(buffer, 0);
            while (!operation.isDone());

            channel.close();
            oos.close();
            baos.close();

        } catch (Exception e) {
            System.err.println("Failed to save the storage in non-volatile memory");
        }
    }

}
