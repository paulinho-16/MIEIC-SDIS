package g24;

import java.io.DataOutputStream;
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
        Utils.log("JOIN", "ALONE");
    }

    public Chord(String ip, int port, String successorIp, int successorPort) {
        this.id = new Identifier(ip, port);
        this.id.setSuccessor(new Identifier(successorIp, successorPort));
        this.id.setPredecessor(new Identifier());

        this.initFingerTable();

        this.join(this.id.getSuccessor());
        Utils.log("JOIN", "SUCCESSOR " + this.id.getSuccessor().toString());
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

        byte[] response = sendMessage(nextNode.getIp(), nextNode.getPort(), 3000, null, "FINDSUCCESSOR", newNode.toString());

        String s = new String(response);

        if (s.equals("NOT_FOUND") || response.length == 0)
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

        if(node.equals(this.id))
            return this.id.getPredecessor();
        
        byte[] response = sendMessage(node.getIp(), node.getPort(), 0, null, "GETPREDECESSOR");

        String[] s = new String(response).split(" ");

        if (s.length < 2)
            return new Identifier();

        return new Identifier(s[0], Integer.parseInt(s[1]));
    }

    // Called periodically, verifies n's immediate successor, and tells the
    // successor about n
    public void stabilize() {

        Utils.log("PERIODICALLY", "STABILIZE");
        Identifier x = this.getPredecessor(this.id.getSuccessor());
        if (!x.equals(new Identifier()) && x.between(this.id, this.id.getSuccessor())) {
            this.id.setSuccessor(x);
            this.fingerTable.put(1, x);
            Utils.log("STABILIZE", "NEW SUCCESSOR " + this.id.getSuccessor().toString());
        }

        this.notifySuccessor();
    }

    // Send NOTIFY this.id to this.id.successor
    public void notifySuccessor() {

        Identifier successor = this.id.getSuccessor();
        sendMessage(successor.getIp(), successor.getPort(), 0, null, "NOTIFY", "P", this.id.getIp(), Integer.toString(this.id.getPort()));
    }

    // lastPredecessor thinks it might be our predecessor
    public void notifyPredecessor(Identifier lastPredecessor) {

        Identifier predecessor = this.id.getPredecessor();
        if (predecessor.equals(new Identifier()) || lastPredecessor.between(predecessor, this.id)) {
            this.id.setPredecessor(lastPredecessor);
            Utils.log("NOTIFY", "NEW PREDECESSOR " + lastPredecessor.toString());
        }
    }

    public void notifySuccessor(Identifier lastSuccessor) {
        this.id.setSuccessor(lastSuccessor);
        this.fingerTable.put(1, lastSuccessor);
        Utils.log("NOTIFY", "NEW SUCCESSOR " + lastSuccessor.toString());
    }

    // Called periodically, refreshes finger table entries, next stores the index of
    // the next finger to fix
    public void fixFingers() {

        try {
            Utils.log("PERIODICALLY", "FIX FINGERS");
            this.next++;
            if (this.next > Utils.m)
                this.next = 2;

            Identifier finger = findSuccessor(this.id.getNext(this.next));
            this.fingerTable.put(this.next, finger);
            Utils.log("FIX FINGERS", "PUT ( " + this.next + " , " + finger.toString() + " )");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    // Called periodically, checks whether predecessor has failed
    public void checkPredecessor() {

        Utils.log("PERIODICALLY", "CHECK PREDECESSOR");
        if (this.hasFailed(this.id.getPredecessor())) {
            this.id.setPredecessor(new Identifier());
            Utils.log("CHECK PREDECESSOR", "HAS FAILED");
        }
    }

    // Notify the Predecessor that this peer is leaving the network, so inform your successor to the current predecessor
    public void notifyPredecessor() {

        Identifier predecessor = this.id.getPredecessor();
        sendMessage(predecessor.getIp(), predecessor.getPort(), 0, null, "NOTIFY", "S", this.id.getSuccessor().getIp(), Integer.toString(this.id.getSuccessor().getPort()));
    }

    public boolean hasFailed(Identifier node) {
        byte[] response = new byte[0];

        if (!node.equals(new Identifier()))
            response = sendMessage(node.getIp(), node.getPort(), 500, null, "ONLINE");

        return response.length == 0;
    }

    public void notifyLeaving() {
        Identifier successor = this.id.getSuccessor();
        Identifier predecessor = this.id.getPredecessor();
        byte[] response = this.sendMessage(successor.getIp(), successor.getPort(), 500, null, "NOTIFY", "L", predecessor.getIp(), Integer.toString(predecessor.getPort())); 
        this.notifyPredecessor();
    }

    public byte[] sendMessage(String ip, int port, int timeout, byte[] body, String... headerString) {

        if (new Identifier(ip, port).equals(this.id))
            return new byte[0];

        byte[] message;

        if(body != null) {
            message = this.makeMessage(body, headerString);
        }
        else {
            message = this.makeHeader(headerString);
        }

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(ip, port);
            socket.setEnabledCipherSuites(Utils.CYPHER_SUITES);
            socket.startHandshake();

            if (timeout > 0) {
                socket.setSoTimeout(timeout);
            }

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeInt(message.length);
            out.write(message, 0, message.length);
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] response = new byte[Utils.FILE_SIZE + 200];
            byte[] aux = new byte[Utils.FILE_SIZE + 200];
            int bytesRead = 0;
            int counter = 0;

            while((bytesRead = in.read(response)) != -1) {
                System.arraycopy(response, 0, aux, counter, bytesRead);
                counter += bytesRead;
            }

            in.close();
            out.close();
            socket.close();

            byte[] result = new byte[counter];
            System.arraycopy(aux, 0, result, 0, counter);
            
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public Identifier getId() {
        return this.id;
    }

    public void getSummary() {
        Utils.log("PERIODICALLY", "SUMMARY");
        Utils.log("SUMMARY", this.id.getSummary());
        Utils.log("SUMMARY", this.fingerTable.toString());
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
