package client;

import peer.PeerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {
    public static void main(String[] args) throws RemoteException {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> [opnd_1] [opnd_2]");
            System.out.println("\t<peer_ap> -> peer's access point");
            System.out.println("\t<sub_protocol> -> operation the peer of the backup service must execute (BACKUP / RESTORE / DELETE / RECLAIM / STATE)");
            System.out.println("\t[opnd_1] -> path name of the file to backup/store/delete or maximum amount of disk space (kB) that the service can use to store the chunks, on reclaim");
            System.out.println("\t[opnd_2] -> integer that specifies the desired replication degree (only for backup)");
            return;
        }

        // Creating RMI
        String peer_ap = args[0];
        PeerInterface peer;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            peer = (PeerInterface) registry.lookup(peer_ap);
        } catch (Exception e) {
            System.err.println("Error creating registry: rmiregistry or the Peer might not have been initialized");
            System.err.println("Exception: " + e);
            return;
        }

        // Parsing arguments according to the protocol requested
        String sub_protocol = args[1];
        String file_path;
        int disk_space, replication_degree;
        switch (sub_protocol) {
            case "BACKUP" -> {
                if (args.length != 4) {
                    System.err.println("BACKUP operation requires 4 arguments: <peer_ap> <sub_protocol> <file_path> <rep_degree>");
                    return;
                }
                file_path = args[2];
                try {
                    replication_degree = Integer.parseInt(args[3]);
                } catch (Exception e) {
                    System.err.println("Fourth argument, <rep_degree>, must be an integer");
                    return;
                }
                peer.backup(file_path, replication_degree);
            }
            case "RESTORE" -> {
                if (args.length != 3) {
                    System.err.println("RESTORE operation requires 3 arguments: <peer_ap> <sub_protocol> <file_path>");
                    return;
                }
                file_path = args[2];
                peer.restore(file_path);
            }
            case "DELETE" -> {
                if (args.length != 3) {
                    System.err.println("DELETE operation requires 3 arguments: <peer_ap> <sub_protocol> <file_path>");
                    return;
                }
                file_path = args[2];
                peer.delete(file_path);
            }
            case "RECLAIM" -> {
                if (args.length != 3) {
                    System.err.println("RECLAIM operation requires 3 arguments: <peer_ap> <sub_protocol> <disk_space>");
                    return;
                }
                try {
                    disk_space = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    System.err.println("Third argument, <disk_space>, must be an integer");
                    return;
                }
                if (disk_space < 0) {
                    System.err.println("Third argument, <disk_space>, must be an integer greater than or equal to 0");
                    return;
                }
                peer.reclaim(disk_space);
            }
            case "STATE" -> {
                if (args.length != 2) {
                    System.err.println("STATE operation requires 2 arguments: <peer_ap> <sub_protocol>");
                    return;
                }
                System.out.println(peer.state());
            }
            default -> System.err.println("Invalid operation. Options: (BACKUP / RESTORE / DELETE / RECLAIM / STATE)");
        }
    }
}