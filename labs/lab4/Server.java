import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("\nUsage: java Server <port number>\n");
            System.out.println("<port number> -> port number the server shall use to provide the service");
            return;
        }
        
        // Parse Arguments
        int port;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <port> must be an integer\n");
            return;
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        }catch(IOException e){
            System.out.println("Unable to listen to port " + port);
        }

        Database db = new Database();

        // Server Cycle waiting for message
        while(true) {
            // Receive Request
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch(IOException e){
                System.out.println("Failed to accept server socket message from port" + port);
                return;
            }
            // Create input and output streams
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Receive a request from the client
            String request = in.readLine();

            // Process Request and return response
            String responseData;
            try {
                responseData = parseResponse(request, db);
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }

            out.println(responseData);

            out.close();
            in.close();
            clientSocket.close();
        }

        //serverSocket.close();
    }

    public static String parseResponse(String commandPacket, Database db) throws Exception {
        //Parse Command Packet
        commandPacket = commandPacket.trim(); // Removes whitespaces
        String printMessage = "";
        String[] splitPacket = commandPacket.split(" "); // Splits the message by space and stores each separate String into an Array
        String out;

        for(String str : splitPacket) {
            printMessage += " " + str;
        }

        if(splitPacket.length == 3 && splitPacket[0].equalsIgnoreCase("REGISTER")) {
            out = db.register(new Dns(splitPacket[1], splitPacket[2])).toString();
        }
        else if(splitPacket.length == 2 && splitPacket[0].equalsIgnoreCase("LOOKUP")) {
            out = db.lookup(splitPacket[1]);
        }
        else {
            out = "NON_EXISTANT_COMMAND";
        }

        System.out.println(printMessage + " :: " + out);
        return out;
    }
}