import java.io.*;
import java.net.*;

public class Client {
    public final static int MAX_SIZE = 256;

    public static void main(String[] args) {
        if (args.length < 4 || args.length > 5) {
            System.out.println("\nUsage: java Client <mcast_addr> <mcast_port> <oper> <opnd>*\n");
            System.out.println("<mcast_addr> -> IP address of the multicast group used by the server to advertise its service");
            System.out.println("<mcast_port> -> port number of the multicast group used by the server to advertise its service");
            System.out.println("<oper> -> operation to request from the server: register/lookup");
            System.out.println("<opnd>* -> list of operands of that operation:\n\t<DNS name> <IP address> for register\n\t<DNS name> for lookup\n");
            return;
        }
        
        // Parsing Arguments
        String mcast_addr = args[0];
        try {
            Dns valid_address = new Dns("www.valid.com", mcast_addr);
            if (valid_address == null || mcast_addr.compareTo("224.0.0.0") < 0 || mcast_addr.compareTo("239.255.255.255") > 0)
                throw new Exception();
        }
        catch(Exception e) {
            System.out.println("Invalid Multicast IP Address. Must be in range '224.0.0.0' to '239.255.255.255'\n");
            return;
        }
        
        int mcast_port;
        try {
            mcast_port = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException e) {
            System.out.println("Argument <mcast_port> must be an integer\n");
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

        // Creating UDP Multicast Socket
        try {
            // Join Multicast group
            MulticastSocket mcast_socket = new MulticastSocket(mcast_port);
            InetAddress group = InetAddress.getByName(mcast_addr);
            mcast_socket.joinGroup(group);

            // Receive Multicast Message
            byte[] message = new byte[MAX_SIZE];
            DatagramPacket packet = new DatagramPacket(message, message.length);
            mcast_socket.receive(packet);

            // Leave Multicast group
            mcast_socket.leaveGroup(group);

            // Print Multicast Data
            String[] splitPacket = new String(packet.getData(), 0, packet.getLength()).split(":");
            InetAddress srvc_addr = InetAddress.getByName(splitPacket[0]);

            int srvc_port;
            try {
                srvc_port = Integer.parseInt(splitPacket[1]);
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
                System.out.println("ERROR\n");
                return;
            }

            System.out.println("multicast: " + mcast_addr + " " + mcast_port + " " + srvc_addr + " " + srvc_port);

            // Send Request
            DatagramSocket requestSocket = new DatagramSocket();
            packet = new DatagramPacket(data, data.length, srvc_addr, srvc_port);
            requestSocket.send(packet);
            
            // Receive Result
            byte[] result = new byte[MAX_SIZE];
            packet = new DatagramPacket(result, result.length);
            requestSocket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println(operation + " " + dns_name + ip + " :: "  + received);
            requestSocket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println(operation + " " + dns_name + ip + " :: "  + "ERROR");
            return;
        }
    }
}