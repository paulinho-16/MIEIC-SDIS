import java.net.*;

public class Client {
    public final static int MAX_SIZE = 256;

    public static void main(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.out.println("\nUsage: java Client <host> <port> <oper> <opnd>*\n");
            System.out.println("<host> -> DNS name/IP address of the server");
            System.out.println("<port> -> port number where the server is providing service");
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
            System.out.println("Argument <port> must be an integer\n");
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

        byte[] data = request.getBytes();

        // Creating UDP Socket
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            
            // Sending packet
            socket.send(packet);

            // Receive Result
            byte[] result = new byte[MAX_SIZE];
            packet = new DatagramPacket(result, result.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Client: " + operation + " " + dns_name + ip + " : " + received);
            socket.close();
        }
        catch(Exception e) {
            System.out.println("Client: " + operation + " " + dns_name + ip + " : " + "ERROR");
            return;
        }
    }
}