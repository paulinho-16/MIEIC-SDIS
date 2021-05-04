package g24;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import g24.Utils;

public class Identifier {
    private String ip;
    private int port;
    private int id;
    private Identifier successor;
    private Identifier predecessor;

    public Identifier(String ip, int port) {
        this.ip = ip;
        this.port = port;

        String key = ip + ":" + port;
        try {
            this.id = Utils.generateHash(key);
        }
        catch(NoSuchAlgorithmException e) {
            this.id = new Random().nextInt() % ((int) Math.pow(2, Utils.m));   
        }
    }

    public Identifier(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getIp(){
        return this.ip;
    }

    public int getPort(){
        return this.port;
    }

    public String toString(){
        return Integer.toString(this.id);
    }

    public int compareTo(Identifier o1) {
        if(o1.id == this.id)
            return 0;
        else if (this.id < o1.id)
            return -1;
        else
            return 1;
    }

    public boolean equals(Identifier o1) {
        return this.compareTo(o1) == 0;
    }

    public boolean lessThan(Identifier o1) {
        return this.compareTo(o1) == -1;
    }

    public Identifier getSuccessor() {
        return successor;
    }

    public void setSuccessor(Identifier successor) {
        this.successor = successor;
    }

    public Identifier getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Identifier predecessor) {
        this.predecessor = predecessor;
    }

    public boolean between(Identifier o1, Identifier o2) {
        return o1.lessThan(this) && this.lessThan(o2);
    }

    public Identifier getNext(int next) {
        return new Identifier((int) (this.id + Math.pow(2, next - 1)));
    }
}
