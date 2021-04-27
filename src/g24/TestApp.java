package g24;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class TestApp {
    public static void main(String[] args){
        if(args.length < 2 || args.length > 4){
            usage();
            return;
        }

        String peerAp = args[0];
        String subProtocol = args[1];

        try {
            Registry registry = LocateRegistry.getRegistry();
            IRemote stub = (IRemote) registry.lookup(peerAp);

            switch(subProtocol){
                case "BACKUP":
                    if(args.length != 4){
                       usage();
                       return;
                    }
                    stub.backup(args[2], Integer.parseInt(args[3]));
                    break;
                case "RESTORE":
                    if(args.length != 3){
                        usage();
                        return;
                    }
                    stub.restore(args[2]);
                    break;
                case "DELETE":
                    if(args.length != 3){
                        usage();
                        return;
                    }
                    stub.delete(args[2]);
                    break;
                case "STATE":
                    if(args.length != 2){
                        usage();
                        return;
                    }
                    String result = stub.state();
                    System.out.println(result);
                    break;
                default:
                    Utils.error("Invalid sub protocol");
                    System.exit(1);
            }
        
        } catch(NumberFormatException e){
            Utils.error("Invalid operands");
            System.exit(1);
        }
        catch(Exception e) {
            Utils.error("Remote Object Exception");
            System.exit(1);
        }
        
    }

    public static void usage(){
        System.err.println("Usage: java TestApp <peerAp> <subProtocol> <opnd_1> <opnd_2>");
    }
}