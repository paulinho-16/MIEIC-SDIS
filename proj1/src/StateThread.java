public class StateThread implements Runnable {
    private volatile String string;

    @Override
    public void run(){
        string = Peer.getData().displayState();
    }

    public String getString(){
        return string;
    }
}
