public class Dns {
    private String hostname;
    private String address;

    final static String validHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
    final static String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    
    public Dns(String hostname, String address) throws Exception {

        if (!hostname.matches(validHostnameRegex) || !address.matches(validIpAddressRegex)) {
            throw new Exception("Invalid Hostname/Address given!!");
        }
        this.hostname = hostname;
        this.address = address; 
    }

    public Dns(String hostname) throws Exception {

        if(!hostname.matches(validHostnameRegex)) {
            throw new Exception("Invalid Hostname");
        }
        this.hostname = hostname;
    }

    public String getHostname() {
        return hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setHostname(String hostname) throws Exception {
        if(!hostname.matches(validHostnameRegex)) {
            throw new Exception("Invalid Hostname given!!");
        }
        this.hostname = hostname;
    }

    public void setAddress(String address) throws Exception {
        if(!address.matches(validIpAddressRegex)) {
            throw new Exception("Invalid Address given!!");
        }
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dns that = (Dns) o;

        return getHostname().equals(that.getHostname());
    }

    @Override
    public int hashCode() {
        int result = getHostname().hashCode();
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }
}
