package ro.lockdowncode.eyedread;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public class Utils {

    public enum Document {
        BULETIN(1),
        PASAPORT(2),
        PERMIS(3);

        private int type;
        Document(int type) {
            this.type = type;
        }
        public int getType() {
            return type;
        }

        public static Document getById(String id) {
            switch (id) {
                case "1": return Document.BULETIN;
                case "2": return Document.PASAPORT;
                case "3": return Document.PERMIS;
            }
            return null;
        }

        public static String getTitle(Document d) {
            switch (d) {
                case BULETIN: return "CARTE DE IDENTITATE";
                case PASAPORT: return "PASAPORT";
                case PERMIS: return "PERMIS DE CONDUCERE";
            }
            return "";
        }
    }

    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

}
