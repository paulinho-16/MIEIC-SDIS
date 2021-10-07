import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public final static int MAX_SIZE = 256;

    public static void main(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.out.println("\nUsage: java Client <host_name> <remote_object_name> <oper> <opnd>*\n");
            System.out.println("<host_name> -> name of the host where the server runs");
            System.out.println("<remote_object_name> -> name the server bound the remote object to");
            System.out.println("<oper> -> operation to request from the server: register/lookup");
            System.out.println("<opnd>* -> list of operands of that operation:\n\t<DNS name> <IP address> for register\n\t<DNS name> for lookup\n");
            return;
        }
        
        // Parsing Arguments
        String host = args[0];
        if (!host.matches(Dns.validHostnameRegex)) {
            System.out.println("Invalid <host_name> parameter.\n");
            return;
        }
        String remote_obj_name = args[1];
        String operation = args[2];
        String dns_name = args[3], ip = "", request;

        if (operation.equalsIgnoreCase("REGISTER") && args.length == 5) {
            ip = " " + args[4];
            request = "REGISTER " + dns_name + ip;
        }
        else if (operation.equalsIgnoreCase("LOOKUP") && args.length == 4) {
            request = "LOOKUP " + dns_name;
        }
        else {
            System.out.println("\nInvalid operation. Possible operations: register, lookup. Check opnd number\n");
            return;
        }

        byte[] data = request.getBytes();

        // Invoking the remote operation on the remote object (RMI)
        try {
            // Obtain the stub for the registry on the server's host
            Registry registry = LocateRegistry.getRegistry(host);

            // Look up the remote object's stub by name in the registry
            RemoteInterface stub = (RemoteInterface) registry.lookup(remote_obj_name);

            // Call the object method
            String response = stub.parseResponse(request);
            System.out.println(operation + " " + dns_name + ip + " :: "  + response);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(operation + " " + dns_name + ip + " :: "  + "ERROR");
            return;
        }
    }
}