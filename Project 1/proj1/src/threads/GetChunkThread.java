package threads;

import peer.Peer;
import storage.Chunk;
import storage.DataStored;

import java.io.FileOutputStream;
import java.io.IOException;

// Called to restore a file after all chunks have been received
public class GetChunkThread implements Runnable {
    String path, fileID;
    int numberChunks;

    public GetChunkThread(String path, String fileID, int numberChunks) {
        this.path = path;
        this.fileID = fileID;
        this.numberChunks = numberChunks;
    }

    // Add _copy to the file name
    public String makeCopyName() {
        String[] newTokens = path.split("/");
        String filename = newTokens[newTokens.length - 1];

        String[] parts = filename.split("\\.");
        String baseName = parts[0];
        if (parts.length > 1) {
            String extension = parts[parts.length-1];
            return baseName + "_copy." + extension;
        }

        return baseName + "_copy";
    }

    // Starts restoring the file with the received chunks
    @Override
    public void run() {
        // Create the file to be restored
        String fileCopy = makeCopyName();
        String copyPath = Peer.getRestoredFilesPath() + "/" + fileCopy;
        DataStored.createFile(copyPath);

        // Start Writing the chunks to the copy file
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(copyPath);
            // Traverse the chunks in sequential order to restore the file
            for (int i = 0; i < numberChunks; i++) {
                String chunkID = fileID + "-" + i;
                // Check if the chunk has been received
                if(!Peer.getData().hasReceivedChunk(chunkID)) {
                    System.err.println("Error: Chunk number " + i + " missing.");
                    return;
                }
                // Write the chunk data to the file and remove it from the list of received chunks
                Chunk chunk = Peer.getData().getReceivedChunk(chunkID);
                Peer.getData().removeReceivedChunk(chunkID);
                fout.write(chunk.getData());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating file: " + copyPath);
        } finally {
            // Close the FileOutputStream after writing the whole file data
            try {
                assert fout != null;
                fout.flush();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("File recovered: " + fileCopy);
    }
}
