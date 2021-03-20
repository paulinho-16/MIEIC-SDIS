import java.io.File;

public class GetChunkThread implements Runnable {
    String path, fileID, senderID;
    int numberChunks;

    public GetChunkThread(String path, String fileID, String senderID, int numberChunks) {
        this.path = path;
        this.fileID = fileID;
        this.senderID = senderID;
        this.numberChunks = numberChunks;
    }

    public String makeCopyName() {
        String[] tokens = path.split("\\.(?=[^\\.]+$)");
        String baseName = tokens[0];
        String extension = tokens[1];

        return baseName + "_copy." + extension;
    }

    @Override
    public void run() {
        String fileCopy = makeCopyName();
        String copyPath = "peers/" + Peer.getPeerID() + "/restored_files/" + fileCopy;

        File file = new File(copyPath);

        // TODO
    }
}
