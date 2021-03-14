import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

// MDB Channel
public class MulticastDataChannel extends MulticastChannel {
    public MulticastDataChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr, port, peerID);
    }

    private void backup(String filePath, int replicationDegree, String version) throws IOException{
        if(filePath == null && replicationDegree < 1){
            throw new IllegalArgumentException("Invalid filepath or replicationDegree");
        }

        // Creating the file based on the filename
        File file = createFile(filePath);

        FileInputStream in = new FileInputStream(file);

        System.out.println("Initializing ");
        int chunkCount = 0;
        byte[] chunk;
        int availableBytes;
        String fileId = this.createId(filePath, this.peerID);

        // While there are still bytes that can be read
        while((availableBytes = in.available()) > 0){
            // Reading the correct amount of bytes
            if(availableBytes > Peer.CHUNK_SIZE) {
                chunk = new byte[Peer.CHUNK_SIZE];
            } else {
                chunk = new byte[availableBytes];
            }
            in.read(chunk);

            byte[] header =  MessageParser.makeHeader(version, "PUTCHUNK", this.peerID , fileId, chunkCount, replicationDegree, chunk);

            chunkCount++;
        }
    }

    public File createFile(String filePath) throws IOException {
        try{
            File myObj = new File(filePath);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + filePath);
                return myObj;
            } else {
                System.out.println("File already exists.");
            }
        }catch(IOException e){
            System.out.println("Error on creating file: " + filePath);
            e.printStackTrace();
        }
    }
}