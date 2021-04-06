import java.io.FileOutputStream;
import java.io.IOException;

public class GetChunkThread implements Runnable {
    String path, fileID, senderID;
    int numberChunks;

    public GetChunkThread(String path, String fileID, String senderID, int numberChunks) {
        this.path = path;
        this.fileID = fileID;
        this.senderID = senderID;
        this.numberChunks = numberChunks;
    }

    // https://stackoverflow.com/questions/4545937/java-splitting-the-filename-into-a-base-and-extension
    public String makeCopyName() {
        String[] tokens = path.split("\\.(?=[^\\.]+$)");
        String[] pathLevels = tokens[0].split(".+?/(?=[^/]+$)");
        String baseName = pathLevels[1];
        String extension = tokens[1];

        return baseName + "_copy." + extension;
    }

    @Override
    public void run() {
        String fileCopy = makeCopyName();
        String copyPath = "peers/" + Peer.getPeerID() + "/restored_files/" + fileCopy;

        DataStored.createFile(copyPath);

        while(!Peer.getData().receivedAllChunks(fileID)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < numberChunks; i++) {
            String chunkID = fileID + "-" + i;
            if(!Peer.getData().hasReceivedChunk(chunkID)) {
                System.out.println("Error: Chunk number " + i + " missing.");
                return;
            }
            Chunk chunk = Peer.getData().getReceivedChunk(chunkID);
            Peer.getData().removeReceivedChunk(chunkID);

            try {
                FileOutputStream fout = new FileOutputStream(copyPath,true);
                fout.write(chunk.getData());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error creating file: " + copyPath);
            }
        }

        System.out.println("File recovered: " + fileCopy);
    }
}
