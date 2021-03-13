import java.io.*;
import java.net.*;

public class Client {
    public final static int MAX_SIZE = 256;

    public static void main(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.out.println("\nUsage: java Client <host_name> <port_number> <oper> <opnd>*\n");
            System.out.println("<host_name> -> name of the host where the server runs");
            System.out.println("<port_number> -> port number where the server provides the service");
            System.out.println("<oper> -> operation to request from the server: register/lookup");
            System.out.println("<opnd>* -> list of operands of that operation:\n\t<DNS name> <IP address> for register\n\t<DNS name> for lookup\n");
            return;
        }
        
        // Parsing Arguments
        String host = args[0]; int port;
        try {
            port = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <port_number> must be an integer\n");
            return;
        }
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

        // Creating TCP Socket
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Sending info
            out.println(request);

            // Receive response
            String response = in.readLine();
            System.out.println(operation + " " + dns_name + ip + " :: "  + response);
            out.close();
            in.close();
            socket.close();
        }
        catch(Exception e) {
            System.out.println(operation + " " + dns_name + ip + " :: "  + "ERROR");
            return;
        }
    }
}