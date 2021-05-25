package g24.protocol;

import g24.storage.FileData;
import g24.storage.Storage;
import g24.Chord;
import g24.Identifier;
import g24.Utils;
import g24.handler.BackupHandler;

public class GetKeys extends Handler {

    private Chord chord;
    private Storage storage;
    private Identifier node;

    public GetKeys(Chord chord, Storage storage, String ip, Integer port) {
        this.chord = chord;
        this.storage = storage;
        this.node = new Identifier(ip, port);
    }

    @Override
    public void run() {
        try {
            
            byte[] message;
            message = ("OK").getBytes();
            out.write(message, 0, message.length);
            out.flush();
            out.close();

            for (String fileId : this.storage.getStoredFiles().keySet()) {
                FileData fileData = this.storage.read(fileId);
                Identifier fileKey = new Identifier(Utils.generateHash(fileId));
                Identifier predecessor = this.chord.getId().getPredecessor();
                if (fileKey.between(predecessor, this.node) || fileKey.equals(this.node) || fileKey.equals(predecessor)) {

                    byte[] fileBytes = fileData.getData();
                    int replicationDegree = fileData.getReplicationDegree();
                    byte[] response = this.chord.sendMessage(this.node.getIp(), this.node.getPort(), 1000, fileBytes,
                            "BACKUP", fileData.getFileID(), Integer.toString(replicationDegree));

                    if(replicationDegree > 1){
                        fileData.setReplicationDegree(fileData.getReplicationDegree() - 1);
                        new BackupHandler(this.chord, this.storage, fileData, this.chord.getId().getSuccessor(), fileData.getReplicationDegree() - 1).run();
                    }
                    else{
                        this.storage.removeFileData(fileId);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}