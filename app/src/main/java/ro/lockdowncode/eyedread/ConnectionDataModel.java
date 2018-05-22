package ro.lockdowncode.eyedread;

/**
 * Created by Adi Neag on 21.05.2018.
 */

public class ConnectionDataModel {

    String name;
    String ip;
    String mac;

    public ConnectionDataModel(String name, String ip, String mac) {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }
}
