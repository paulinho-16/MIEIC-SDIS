import java.util.ArrayList;

public class Database {
    private ArrayList<Dns> DnsList;

    public Database() {
        this.DnsList = new ArrayList<>();
    }

    public Integer register(Dns newDns) {
        // Check if the new plate doesn't already exist
        for(Dns dns : this.DnsList) {
            if(dns.equals(newDns)) {
                return -1;
            }
        }
        // Reaches here if there is no equal Dns already registered 
        DnsList.add(newDns);
        return DnsList.size();  
    }

    public String lookup(String hostname) {
        try {
            Dns comparisonDns = new Dns(hostname);
            // Searching for Dns with the given hostname, returning its IP address if found
            for(Dns dns : this.DnsList) {
                if(comparisonDns.equals(dns)) {
                    return hostname + " " + dns.getAddress();
                }
            }
            // Return NOT_FOUND if there is no Dns with given hostname
            return "NOT_FOUND";
        }
        catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
