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
                boolean toSend = false;

                if(fileKey.between(predecessor, this.node) || fileKey.equals(this.node)) {
                    toSend = true;
                }
                else {
                    Utils.out("GETKEYS","ASKING predecessor " + fileKey.toString());
                    byte[] response = this.chord.sendMessage(predecessor.getIp(), predecessor.getPort(), 1000, null,
                            "HASFILE", fileData.getFileID());

                    String s = new String(response);
                    if (s.equals("OK")) {
                        toSend = true;
                    }
                }

                Utils.out("GETKEYS", "FILE " + fileKey.toString());

                if(toSend) {
                    Utils.out("GETKEYS", "TO SEND FILE " + fileKey.toString());
                    byte[] fileBytes = fileData.getData();
                    int replicationDegree = fileData.getReplicationDegree();
                    this.chord.sendMessage(this.node.getIp(), this.node.getPort(), 1000, fileBytes,
                            "BACKUP", fileData.getFileID(), Integer.toString(replicationDegree));

                    if(replicationDegree > 1) {
                        fileData.setReplicationDegree(replicationDegree - 1);
                        this.storage.getFile(fileId).setReplicationDegree(replicationDegree - 1);
                        new BackupHandler(this.chord, this.storage, fileData, this.chord.getId().getSuccessor(), fileData.getReplicationDegree() - 1).run();
                    }
                    else {
                        this.storage.removeFileData(fileId);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}