package g24;

import java.io.DataOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.DataInputStream;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.util.concurrent.ConcurrentHashMap;

public class Chord {

    private Identifier id;
    private int next = 1;
    private ConcurrentHashMap<Integer, Identifier> fingerTable;

    public Chord(String ip, int port) {
        this.id = new Identifier(ip, port);
        this.id.setSuccessor(this.id);
        this.id.setPredecessor(new Identifier());

        this.initFingerTable();

    }

    public Chord(String ip, int port, String successorIp, int successorPort) {
        this.id = new Identifier(ip, port);
        this.id.setSuccessor(new Identifier(successorIp, successorPort));
        this.id.setPredecessor(new Identifier());

        this.initFingerTable();

        this.join(this.id.getSuccessor());
    }

    public void initFingerTable() {
        this.fingerTable = new ConcurrentHashMap<>();
        for (int i = Utils.m; i >= 1; i--) {
            this.fingerTable.put(i, new Identifier());
        }
        this.fingerTable.put(1, this.id.getSuccessor());
    }

    // Join a Chord ring containing node knownNode
    public void join(Identifier knownNode) {
        Identifier x = this.findSuccessor(knownNode, this.id);
        this.id.setSuccessor(x);
        this.fingerTable.put(1, x);
    }

    public Identifier findSuccessor(Identifier nextNode, Identifier newNode) {

        byte[] response = sendMessage(nextNode.getIp(), nextNode.getPort(), 0, null, "FINDSUCCESSOR", newNode.toString());

        // Receive IP and Port from successor
        String s = new String(response);

        if (s.equals("NOT_FOUND"))
            return this.id;

        String[] splitResponse = s.split(" ");

        return new Identifier(splitResponse[0], Integer.parseInt(splitResponse[1]));
    }

    // Ask node n to find the successor of id
    public Identifier findSuccessor(Identifier newNode) {

        Identifier successor = this.id.getSuccessor();

        if (newNode.between(this.id, successor) || newNode.equals(successor)) {
            return successor;
        } else {
            Identifier nextNode = this.closestPrecedingNode(newNode);

            if (nextNode.equals(this.id))
                return this.id;

            return this.findSuccessor(nextNode, newNode);
        }
    }

    // Search the local table for the highest predecessor of newNode
    public Identifier closestPrecedingNode(Identifier newNode) {

        for (int i = Utils.m; i >= 1; i--) {
            Identifier finger = this.fingerTable.get(i);
            if (!finger.equals(new Identifier()) && finger.between(this.id, newNode)) {
                return finger;
            }
        }

        return this.id;
    }

    public Identifier getPredecessor(Identifier node) {

        if(node.equals(this.id)) return this.id.getPredecessor();
        
        // Send GETPREDECESSOR newNode to nextNode
        byte[] response = sendMessage(node.getIp(), node.getPort(), 0, null, "GETPREDECESSOR");

        // Receive IP and Port from successor
        String s = new String(response);

        if (s.equals("NOT_FOUND"))
            return new Identifier();

        String[] splitResponse = s.split(" ");

        // response => ip port
        return new Identifier(splitResponse[0], Integer.parseInt(splitResponse[1]));
    }

    // Called periodically, verifies n's immediate successor, and tells the
    // successor about n
    public void stabilize() {
        
        // System.out.println("PERIODICALLY: STABILIZE");
        // Mandar mensagem ao sucessor a perguntar pelo predecessor
        Identifier x = this.getPredecessor(this.id.getSuccessor());
        if (!x.equals(new Identifier()) && x.between(this.id, this.id.getSuccessor())) {
            this.id.setSuccessor(x);
            this.fingerTable.put(1, x);
        }

        this.notifySuccessor();
    }

    // Send NOTIFY this.id to this.id.successor
    public void notifySuccessor() {
        Identifier successor = this.id.getSuccessor();
        byte[] response = sendMessage(successor.getIp(), successor.getPort(), 0, null, "NOTIFY", this.id.getIp(),
                Integer.toString(this.id.getPort()));
    }

    // lastPredecessor thinks it might be our predecessor
    public void notify(Identifier lastPredecessor) {
        Identifier predecessor = this.id.getPredecessor();
        if (predecessor.equals(new Identifier()) || lastPredecessor.between(predecessor, this.id)) {
            this.id.setPredecessor(lastPredecessor);
        }
    }

    // Called periodically, refreshes finger table entries, next stores the index of
    // the next finger to fix
    public void fix_fingers() {

        // System.out.println("PERIODICALLY: FIX FINGERS");

        this.next++;
        if (this.next > Utils.m)
            this.next = 1;

        this.fingerTable.put(this.next, findSuccessor(this.id.getNext(this.next)));

        System.out.println("ID " +  this.id.toString() + ": " + this.fingerTable.toString() + " GETNEXT " + this.id.getNext(this.next).toString());
    }

    // Called periodically, checks whether predecessor has failed
    public void checkPredecessor() {

        // System.out.println("PERIODICALLY: CHECK PREDECESSOR");

        if (this.hasFailed(this.id.getPredecessor())) {
            this.id.setPredecessor(new Identifier());
            // System.out.println("PREDECESSOR FAILED");
        }
    }

    public boolean hasFailed(Identifier node) {
        // Send ONLINE to node with delay of 500ms
        // System.out.println("PERIODICALLY: HAS FAILED");
        byte[] response = new byte[0];

        if (!node.equals(new Identifier()))
            response = sendMessage(node.getIp(), node.getPort(), 500, null, "ONLINE");

        return response.length == 0;
    }

    public byte[] sendMessage(String ip, int port, int timeout, byte[] body, String... headerString) {

        if (new Identifier(ip, port).equals(this.id))
            return new byte[0];

        byte[] message;

        if(body != null){
            System.out.println("Body Length = " + body.length);
            message = this.makeMessage(body, headerString);
        }
        else{
            message = this.makeHeader(headerString);
        }

        // System.out.println("SEND: " + String.join(" ", headerString));
        // System.out.println("--------------------------------");

        try {
            // Initialize Sockets
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
            socket.setEnabledCipherSuites(Utils.CYPHER_SUITES);
            socket.startHandshake();

            if (timeout > 0) {
                socket.setSoTimeout(timeout);
            }

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.write(message, 0, message.length);
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] response = new byte[1000];
            int bytesRead = in.read(response);
            byte[] short_data = new byte[bytesRead];
            System.arraycopy(response, 0, short_data, 0, bytesRead);

            // System.out.println("RECEIVED: " + new String(response));
            // System.out.println("--------------------------------");

            in.close();
            out.close();
            socket.close();

            return short_data;
        } catch (SocketTimeoutException e){
            System.err.println("No response from peer");
            return new byte[0];
        }
        catch (SocketException e) {
            return new byte[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public Identifier getId() {
        return this.id;
    }

    public ConcurrentHashMap<Integer, Identifier> getFingerTable() {
        return this.fingerTable;
    }

    public void getSummary() {
        System.out.println(this.id.summary());
    }

    private byte[] makeMessage(byte[] body, String... headerString) {
        byte[] header = (String.join(" ", headerString) + Utils.CRLF + Utils.CRLF).getBytes();
        byte[] message = new byte[header.length + body.length];
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(body, 0, message, header.length, body.length);

        return message;
    }

    // Create a message with header only, no body
    private byte[] makeHeader(String... headerString) {
        return (String.join(" ", headerString) + Utils.CRLF + Utils.CRLF).getBytes();
    }

}
