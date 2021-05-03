public class Identifier {
    private String ip;
    private int port;
    private int id;
    private Identifier successor;
    private Identifier predecessor;

    public Identifier(String ip, int port){
        // String id = ip + ":" + port;

        // Hash 
    }

    public int getId() {
        return this.id;
    }

    public int compareTo(Identifier o1) {
        if(o1.id == this.id)
            return 0;
        else if (this.id < o1.id)
            return -1;
        else
            return 1;
    }

    public boolean equals(Identifier o1){
        return this.compareTo(o1) == 0;
    }

    public boolean lessThan(Identifier o1){
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
}
