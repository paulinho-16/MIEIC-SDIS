package g24;

import java.io.DataOutputStream;
import java.net.SocketTimeoutException;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class Chord {
    
    private Identifier id;
    private int next = 1;
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

        for (int i = Utils.m; i >= 1; i--) {
            this.fingerTable.put(i,null);
        }

        this.join(this.id.getSuccessor());
    }


    // Join a Chord ring containing node knownNode
    public void join(Identifier knownNode) {
        this.id.setSuccessor(this.findSuccessor(knownNode, this.id));
    }

    public Identifier findSuccessor(Identifier nextNode, Identifier newNode) {
        // Send FINDSUCCESSOR newNode to nextNode
        byte [] response =  sendMessage(nextNode.getIp(), nextNode.getPort(), false, "FINDSUCCESSOR", this.id.toString());
        
        // Receive IP and Port from successor
        String s = new String(response);
        String[] splitResponse = s.split(" ");

        // response => ip port
        return new Identifier(splitResponse[0], Integer.parseInt(splitResponse[1]));
    }

    // Ask node n to find the successor of id
    public Identifier findSuccessor(Identifier newNode) {
        
        Identifier successor = this.id.getSuccessor();
        
        if (newNode.between(this.id, successor) || newNode.equals(successor)) {
            return successor;
        }
        else {
            Identifier nextNode = this.closestPrecedingNode(newNode);
            return this.findSuccessor(nextNode, newNode);
        }
    }

    // Search the local table for the highest predecessor of newNode
    public Identifier closestPrecedingNode(Identifier newNode) {
                
        for (int i = Utils.m; i >= 1; i--) {
            Identifier finger = this.fingerTable.get(i);
            if(finger != null && finger.between(this.id, newNode)) {
                return finger;
            }
        }
        
        return this.id;
    }

    public Identifier getPredecessor(Identifier node) {
        // Send GETPREDECESSOR newNode to nextNode
        byte [] response =  sendMessage(node.getIp(), node.getPort(), false, "GETPREDECESSOR");

        // Receive IP and Port from successor
        String s = new String(response);
        String[] splitResponse = s.split(" ");

        // response => ip port
        return new Identifier(splitResponse[0], Integer.parseInt(splitResponse[1]));
    }

    // Called periodically, verifies n's immediate successor, and tells the successor about n
    public void stabilize() {
        // Mandar mensagem ao sucessor a perguntar pelo predecessor
        Identifier x = this.getPredecessor(this.id.getSuccessor());
        if(x.between(this.id, this.id.getSuccessor())) {
            this.id.setSuccessor(x);
        }
        // This notify does not exist
        this.notifySuccessor();
    }

    // Send NOTIFY this.id to this.id.successor
    public void notifySuccessor() {
        Identifier successor = this.id.getSuccessor();
        byte [] response =  sendMessage(successor.getIp(), successor.getPort(), false,"NOTIFY", this.id.toString());
    }

    // lastPredecessor thinks it might be our predecessor
    public void notify(Identifier lastPredecessor) {
        Identifier predecessor = this.id.getPredecessor();
        if(predecessor == null || lastPredecessor.between(predecessor, this.id)) {
            this.id.setPredecessor(lastPredecessor);
        }
    }

    // Called periodically, refreshes finger table entries, next stores the index of the next finger to fix
    public void fix_fingers() {
        this.next++;
        if (this.next > Utils.m)
            this.next = 1;
    
        this.fingerTable.put(this.next, findSuccessor(this.id.getNext(this.next)));
    }

    // Called periodically, checks whether predecessor has failed
    public void checkPredecessor() {
        if (this.hasFailed(this.id.getPredecessor()))
            this.id.setPredecessor(null);
    }

    public boolean hasFailed(Identifier node) {
        // TODO: mandar mensagem pro node a ver se ele ta online com delay de 500 ms
        // Send ONLINE to node
        byte[] response =  sendMessage(node.getIp(), node.getPort(), true, "ONLINE");

        return response.length == 0;
    }

    public byte[] sendMessage(String ip, int port, boolean timeout, String... headerString) {

        byte[] message = (String.join(" ", headerString) + Utils.CRLF + Utils.CRLF).getBytes();

        try {
            // Initialize Sockets
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
            socket.startHandshake();
            if(timeout) {
                socket.setSoTimeout(500);
            }
            
            // Send CHUNK message through the port
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(message, 0, message.length);
            
            // Receive the response
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] response = in.readAllBytes();
            
            // Closing buffers
            out.flush();
            out.close();
            in.close();
            socket.close();

            return response;
        } catch (SocketTimeoutException e) {
            return new byte[0];
        }  catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public Identifier getId(){
        return this.id;
    }
}
