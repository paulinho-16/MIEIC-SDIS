package g24;

import java.util.concurrent.ConcurrentHashMap;

public class Chord {
    
    private Identifier id;
    private int m;
    private ConcurrentHashMap<Integer,Identifier> fingerTable;

    public Chord(String ip, int port) {

        this.id = new Identifier(ip, port);
        this.id.setSuccessor(this.id);
        this.fingerTable = new ConcurrentHashMap<>();
    }

    public Chord(String ip, int port, String successorIp, int successorPort) {

        this.id = new Identifier(ip, port);        
        this.id.setSuccessor(new Identifier(successorIp, successorPort));
        this.fingerTable = new ConcurrentHashMap<>();

        for (int i = this.m - 1; i >= 0; i--) {
            this.fingerTable.put(i,null);
        }
    }

    public Identifier findSuccessor(Identifier nextNode, Identifier newNode) {
        // mandar mensagem ao nextNode pra fazer findSuccessor com newNode
        return id;
    }

    // Ask node n to find the successor of id
    public Identifier findSuccessor(Identifier newNode) {
        
        if (this.id.lessThan(newNode) && (newNode.lessThan(this.id.getSuccessor()) || newNode.equals(this.id.getSuccessor()))) {
            return this.id.getSuccessor();
        }
        else{
            Identifier nextNode = this.closestPrecedingNode(newNode);
            return this.findSuccessor(nextNode, newNode);
        }
    }

    // Search the local table for the highest predecessor of id
    public Identifier closestPrecedingNode(Identifier newNode) {
                
        for (int i = this.m - 1; i >= 0; i--) {
            Identifier finger = this.fingerTable.get(i);
            if(finger != null && this.id.lessThan(finger) && finger.lessThan(newNode)) {
                return finger;
            }
        }
        
        return this.id;
    }

    // Join a Chord ring containing node n'
    public void join(Identifier nextNode) {
        this.id.setSuccessor(this.findSuccessor(nextNode, this.id));
    }

    // Called periodically, verifies n's immediate successor, and tells the successor about n
    public void stabilize() {
        // Mandar mensagem ao sucessor a perguntar pelo predecessor
        Identifier x = this.id.getSuccessor().getPredecessor();
        // ...
        
    }

}
