package peer;

import java.io.*;
import java.net.*;

// Class that stores the arguments of the command line that initializes a peer
public class PeerInitializer {
    public static void main(String[] args) throws IOException {
        if(args.length != 9) {
            System.out.println("Usage: java PeerInitializer <protocolVersion> <peerId> <accessPoint>" +
            " <mcAddr> <mcPort> <mdbAddr> <mdbPort> <mdrAddr> <mdrPort>");
            System.out.println("<protocolVersion> -> Version of the protocol to be used");
            System.out.println("<peerId> -> Id of the the peer to be initialized");
            System.out.println("<accessPoint> -> Port identifier + Ip in case of TCP, name in case of RMI"); // We want to implement RMI
            // Mc -> Control Messages between peers
            System.out.println("<mcAddr> -> Multicast address for Control Messages between peers");
            System.out.println("<mcPort> -> Multicast port to for Control Messages between peers");
            // Mdb -> Backup requests between peers
            System.out.println("<mdbAddr> -> Multicast address for Backup requests between peers");
            System.out.println("<mdbPort> -> Multicast port for Backup requests between peers");
            // Mdr -> Restore between peers
            System.out.println("<mdrAddr> -> Multicast address for Restore requests between peers");
            System.out.println("<mdrPort> -> Multicast port for Restore requests between peers");
            return;
        }

        // Version 1.0 -> No enhancements. Other versions may include several enhancements
        // Version 2.0 -> Includes all enhancements (Backup, Restore, Delete)
        String version = args[0];
        String peerId = args[1];
        String accessPoint = args[2];

        // Mc -> Control Messages between peers
        InetAddress mcAddr = InetAddress.getByName(args[3]);
        int mcPort = Integer.parseInt(args[4]);
        // Mdb -> Backup requests between peers
        InetAddress mdbAddr = InetAddress.getByName(args[5]);
        int mdbPort = Integer.parseInt(args[6]);
        // Mdr -> Restore requests between peers
        InetAddress mdrAddr = InetAddress.getByName(args[7]);
        int mdrPort = Integer.parseInt(args[8]);

        // Call a Peer object using all this parameters
        new Peer(version, peerId, accessPoint, mcAddr, mcPort, mdbAddr, mdbPort, mdrAddr, mdrPort);
    }
}
