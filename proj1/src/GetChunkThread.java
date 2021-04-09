import java.io.FileOutputStream;
import java.io.IOException;

public class GetChunkThread implements Runnable {
    String path, fileID;
    int numberChunks;

    public GetChunkThread(String path, String fileID, int numberChunks) {
        this.path = path;
        this.fileID = fileID;
        this.numberChunks = numberChunks;
    }

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

    @Override
    public void run() {
        String fileCopy = makeCopyName();
        String copyPath = Peer.getRestoredFilesPath() + "/" + fileCopy;

        DataStored.createFile(copyPath);
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(copyPath);
            for (int i = 0; i < numberChunks; i++) {
                String chunkID = fileID + "-" + i;
                if(!Peer.getData().hasReceivedChunk(chunkID)) {
                    System.out.println("Error: Chunk number " + i + " missing.");
                    return;
                }
                Chunk chunk = Peer.getData().getReceivedChunk(chunkID);
                Peer.getData().removeReceivedChunk(chunkID);


                fout.write(chunk.getData());
            }

        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating file: " + copyPath);
        }finally {
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
