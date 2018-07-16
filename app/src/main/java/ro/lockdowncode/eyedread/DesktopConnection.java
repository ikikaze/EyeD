package ro.lockdowncode.eyedread;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adi Neag on 13.07.2018.
 */

public class DesktopConnection extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String ip;
    private int status;
    private Date lastConnected;

    public DesktopConnection() {

    }

    public DesktopConnection(String id, String name, String ip) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.status = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(Date lastConnected) {
        this.lastConnected = lastConnected;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
