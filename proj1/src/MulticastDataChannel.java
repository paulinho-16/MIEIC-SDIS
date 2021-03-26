import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

// MDB Channel
public class MulticastDataChannel extends MulticastChannel {
    public MulticastDataChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr, port, peerID);
    }

    public void backup(String path, int replicationDegree, String version) throws IOException {
        if(path == null && replicationDegree < 1) {
            throw new IllegalArgumentException("Invalid filepath or replicationDegree");
        }

        // Creating new file if it doesn't exist
        File file = new File(path);

        FileInputStream in = new FileInputStream(file);

        int chunkCount = 0;
        byte[] chunkData;
        int availableBytes;
        String fileID = this.createId(this.peerID, path, file.lastModified());

        // Add the file to the peer's list of backed up files
        Peer.getData().addNewFileToMap(new FileData(path, fileID, replicationDegree));

        // While there are still bytes that can be read
        while((availableBytes = in.available()) > 0) {
            // Reading the correct amount of bytes
            if(availableBytes > Peer.CHUNK_SIZE) {
                chunkData = new byte[Peer.CHUNK_SIZE];
            } else {
                chunkData = new byte[availableBytes];
            }
            in.read(chunkData);
            byte[] message =  MessageParser.makeMessage(chunkData, version, "PUTCHUNK", this.peerID , fileID, Integer.toString(chunkCount), Integer.toString(replicationDegree));
            // Verify if the peer contains

            //Chunk chunk = Peer.getData().getBackupChunk(fileId, chunkCount);
            //if (chunk == null) {
            //Chunk chunk = new Chunk(version, fileId, chunkCount, chunkData);
            System.out.println("MDB sending :: PUTCHUNK chunk " + chunkCount + " Sender " + this.peerID);
            //Peer.getData().backupNewChunk(chunk);
            System.out.println("First point");
            FileData filedata = Peer.getData().getFileData(fileID);
            System.out.println("Second point");
            String chunkID = fileID + "-" + chunkCount;
            filedata.addChunk(chunkID);
            System.out.println("Third point");

            //Peer.getData().addNewChunkToFileDataMap(chunkCount);

            Peer.executor.execute(new PutChunkThread(message, fileID, chunkCount, replicationDegree));
            //}

            chunkCount++;
        }

        // Caso em que file size é múltiplo de 64kb já está incluído no ciclo de cima? testar.
    }


}