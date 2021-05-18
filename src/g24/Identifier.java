package g24;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

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

    public Identifier() {
        this.id = -1;
    }

    public Identifier(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return Integer.toString(this.id);
    }

    public String getSummary() {
        return "ID: " + this.toString() + " SUCCESSOR: " + this.successor.toString() + " PREDECESSOR: " + this.predecessor.toString();
    }

    public boolean equals(Identifier o1) {
        return this.id == o1.id;
    }

    public boolean lessThan(Identifier o1) {
        return this.id < o1.id;
    }

    public Identifier getSuccessor() {
        return this.successor;
    }

    public void setSuccessor(Identifier successor) {
        this.successor = successor;
    }

    public Identifier getPredecessor() {
        return this.predecessor;
    }

    public void setPredecessor(Identifier predecessor) {
        this.predecessor = predecessor;
    }

    public boolean between(Identifier o1, Identifier o2) {
        
        if (o1.lessThan(o2)) {
            return o1.lessThan(this) && this.lessThan(o2);
        }

        return o1.lessThan(this) || this.lessThan(o2);
    }

    public Identifier getNext(int next) {
        return new Identifier((int) ((this.id + Math.pow(2, next - 1)) % ((int) Math.pow(2, Utils.m))));
    }

    @Override
    public boolean equals(Object o) {
        return this.equals((Identifier) o);
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
