import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server implements RemoteInterface{
    public final static int MAX_SIZE = 256;
    private static Database db; // Declaring database as a static variable to be accesses by all server instances

    public static void main(String[] args) throws IOException, RemoteException {
        if (args.length != 1) {
            System.out.println("\nUsage: java Server <remote_object_name>\n");
            System.out.println("<remote_object_name> -> name the server bound the remote object to");
            return;
        }

        try {
            // Create Remote Object (RMI)
            String remoteObject =  args[0];
            Server obj = new Server();
            RemoteInterface stub =  (RemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObject, stub);

            // Initializing the database
            db = new Database(); 

            System.err.println("Server ready");

        } catch(Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String parseResponse(String request) throws RemoteException,Exception {
        //Parse Command Packet
        request = request.trim(); // Removes whitespaces
        String printMessage = "";
        String out;
        String[] splitPacket = request.split(" "); // Splits the message by space and stores each separate String into an Array

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