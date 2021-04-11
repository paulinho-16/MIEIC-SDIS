package channels;

import messages.MessageParser;
import peer.Peer;
import storage.FileData;
import threads.PutChunkThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

// MDB Channel
public class MulticastDataChannel extends MulticastChannel {
    public MulticastDataChannel(InetAddress addr, int port, String peerID) throws IOException {
        super(addr, port, peerID);
    }

    public void backup(String filename, int replicationDegree) throws IOException {

        if(filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }

        if (replicationDegree < 1) {
            throw new IllegalArgumentException("Invalid replicationDegree: value must be greater or equal to 1");
        }

        // Creating new file if it doesn't exist
        String path = Peer.getPersonalFilesPath() + "/" + filename;

        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalArgumentException("File " + filename + " doesn't belong to this peer.");
        }

        FileInputStream in = new FileInputStream(file);

        int chunkCount = 0;
        byte[] chunkData;
        int availableBytes;
        String fileID = this.createId(this.peerID, path, file.lastModified());

        // Add the file to the peer's list of backed up files
        Peer.getData().addNewFileToMap(new FileData(filename, fileID, replicationDegree));

        // While there are still bytes that can be read
        while((availableBytes = in.available()) > 0) {
            // Reading the correct amount of bytes
            if(availableBytes > Peer.CHUNK_SIZE) {
                chunkData = new byte[Peer.CHUNK_SIZE];
            }
            else {
                chunkData = new byte[availableBytes];
            }
            in.read(chunkData);
            byte[] message =  MessageParser.makeMessage(chunkData, Peer.getVersion(), "PUTCHUNK", this.peerID , fileID, Integer.toString(chunkCount), Integer.toString(replicationDegree));

            FileData filedata = Peer.getData().getFileData(fileID);
            String chunkID = fileID + "-" + chunkCount;

            filedata.addChunk(chunkID);

            Peer.executor.execute(new PutChunkThread(message, fileID, chunkCount, replicationDegree));

            chunkCount++;

            if(availableBytes == Peer.CHUNK_SIZE) {
                byte[] messageChunkData1 =  MessageParser.makeMessage(new byte[0], Peer.getVersion(), "PUTCHUNK", this.peerID , fileID, Integer.toString(chunkCount), Integer.toString(replicationDegree));
                chunkID = fileID + "-" + chunkCount;
                filedata.addChunk(chunkID);
                Peer.executor.execute(new PutChunkThread(messageChunkData1, fileID, chunkCount, replicationDegree));
            }
        }
    }
}