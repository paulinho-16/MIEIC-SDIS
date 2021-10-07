import java.io.*;
import java.net.*;

public class Server {
    public final static int MAX_SIZE = 256;

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("\nUsage: java Server <srvc_port> <mcast_addr> <mcast_port>\n");
            return;
        }

        // Muticast address must be between 224.0.0.0 and 239.255.255.255
        String mcast_addr = args[1];
        try {
            Dns valid_address = new Dns("www.valid.com", mcast_addr);
            if (valid_address == null || mcast_addr.compareTo("224.0.0.0") < 0 || mcast_addr.compareTo("239.255.255.255") > 0)
                throw new Exception();
        }
        catch(Exception e) {
            System.out.println("Invalid Multicast IP Address. Must be in range '224.0.0.0' to '239.255.255.255'\n");
            return;
        }
        
        // Parse port Interger values
        int port;
        int multicastPort;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <port> must be an integer\n");
            return;
        }

        try {
            multicastPort = Integer.parseInt(args[2]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <mcast_port> must be an integer\n");
            return;
        }

        // Declaring bytes to be sent and received
        String msg = "localhost:" + args[0];
        byte[] responseData = msg.getBytes();  // Data to be sent by the server
        byte[] dataReceived; // Data to be received from the clients

        // Initializing Multicast socket
        InetAddress group = InetAddress.getByName(args[1]);  // Endereço Multicast a ser utilizado
        MulticastSocket multicastSocket = new MulticastSocket(); // Socket Multicast a ser utilizada na seua respetiva port
        multicastSocket.setTimeToLive(1);

        // Create message to be send to multicast perdiodically
        DatagramPacket broadcastDatagram= new DatagramPacket(responseData, responseData.length, group, multicastPort);

        // Initializing Unicast Socket
        DatagramSocket socket = new DatagramSocket(port);
        socket.setSoTimeout(2000); // Set message timeout to 2 seconds

        Database db = new Database(); // Initializing DNS database for server storage~

        // Server Cycle waiting for message
        while(true) {
            try {
                // Send Multicast Message
                multicastSocket.send(broadcastDatagram);

                // Receive Request
                dataReceived = new byte[MAX_SIZE];
                DatagramPacket packetReceived =  new DatagramPacket(dataReceived, dataReceived.length);
                
                // Process Request and return response
                // Dar parse à request. Pode ser LOOKUP ou REGISTER
                responseData = new byte[MAX_SIZE];
                socket.receive(packetReceived);
                responseData = parseResponse(new String(packetReceived.getData()), db).getBytes();

                // Sending the parsed packet response to the Client
                DatagramPacket packetSend =  new DatagramPacket(responseData, responseData.length,packetReceived.getAddress(),packetReceived.getPort());
                socket.send(packetSend);  
            }
            catch (SocketTimeoutException e) {
                System.out.println("multicast: " + args[1] + " " + multicastPort + " 127.0.0.1 " + args[0]);
            }
            catch(Exception e2) {
                e2.printStackTrace();
                return;
            }                
        }
        
        //socket.close();
        //multicastSocket.close();
    }

    public static String parseResponse(String commandPacket, Database db) throws Exception {
        //Parse Command Packet
        commandPacket = commandPacket.trim(); // Removes whitespaces
        String printMessage = "";
        String out;
        String[] splitPacket = commandPacket.split(" "); // Splits the message by space and stores each separate String into an Array

        for(String str : splitPacket) {
            printMessage += " " + str;
        }
        
        if(splitPacket.length == 3 && splitPacket[0].equalsIgnoreCase("REGISTER")) {
            out = db.register(new Dns(splitPacket[1], splitPacket[2])).toString();
            System.out.println(printMessage + " :: " + out );
        }
        else if(splitPacket.length == 2 && splitPacket[0].equalsIgnoreCase("LOOKUP")) {
            out = db.lookup(splitPacket[1]);
            System.out.println(printMessage + " :: " + out);
        }
        else {
            return "NON_EXISTANT_COMMAND";
        }
        
        return out;
    }
}