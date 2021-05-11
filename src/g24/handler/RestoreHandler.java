public class RestoreHandler implements Runnable {

    private Chord chord;
    private Identifier receiver;
    private FileData fileData;

    public RestoreHandler(Chord chord, Identifier receiver, FileData fileData){
        this.chord = chord;
        this.receiver = receiver;
        this.fileData = fileData;

    }

    @Override
    public void run(){
        
        // Isto provavelmente vai precisar de um TRY CATCH, portanto alguém que tenha autocomplete que depois trate disso :-) 

        // Send a restore meessage and wait for a reponse containing the file
        byte[] response = this.chord.sendMessage(this.receiver.getIp(), this.receiver.getPort(), 1000, null, "RESTORE",this.fileData);

        // Analyze the response fromthe message
        // Restore the file using the byte[] received from the message

    }
}