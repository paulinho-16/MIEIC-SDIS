import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("\nUsage: java Server <port number>\n");
            System.out.println("<port number> -> port number the server shall use to provide the service");
            return;
        }
        
        // Send request
        int port;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <port> must be an integer\n");
            return;
        }

        DatagramSocket socket = new DatagramSocket(port);

        Database db = new Database();

        byte[] data;
        byte[] responseData;

        // Server Cycle waiting for message
        while(true) {
            // Receive Request
            data = new byte[256];
            DatagramPacket packetReceived =  new DatagramPacket(data, data.length);
            socket.receive(packetReceived);

            // Process Request and return response
            try {
                responseData = parseResponse(new String(packetReceived.getData()), db).getBytes();
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Sending the parsed packet response to the Client
            DatagramPacket packetSend =  new DatagramPacket(responseData, responseData.length, packetReceived.getAddress(), packetReceived.getPort());
            socket.send(packetSend);
        }
        
        //socket.close();
    }

    public static String parseResponse(String commandPacket, Database db) throws Exception {
        //Parse Command Packet
        commandPacket = commandPacket.trim(); // Removes whitespaces
        String printMessage = "Server:";
        String[] splitPacket = commandPacket.split(" "); // Splits the message by space and stores each separate String into an Array

        for(String str : splitPacket) {
            printMessage += " " + str;
        }

        System.out.println(printMessage);
        if(splitPacket.length == 3 && splitPacket[0].equalsIgnoreCase("REGISTER")) {
            return db.register(new Dns(splitPacket[1], splitPacket[2])).toString();
        }
        else if(splitPacket.length == 2 && splitPacket[0].equalsIgnoreCase("LOOKUP")) {
            return db.lookup(splitPacket[1]);
        }
        
        return "NON_EXISTANT_COMMAND";
    }
}