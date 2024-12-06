package android.net.wifi;

import java.net.InetAddress;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(WifiInfo.class)
public class WifiInfoHidden {
    public static final int INVALID_RSSI = -127;
    public static final int UNKNOWN_FREQUENCY = -1;
    public static final int MIN_RSSI = -126;
    public static final int MAX_RSSI = 200;
    public static final int DPM_SECURITY_TYPE_UNKNOWN = -1;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public int score;

    public void setSSID(WifiSsid wifiSsid) {
        throw new RuntimeException("Stub!");
    }

    public void setMacAddress(String macAddress) {
        throw new RuntimeException("Stub!");
    }

    public void setBSSID(String BSSID) {
        throw new RuntimeException("Stub!");
    }

    public void setRssi(int rssi) {
        throw new RuntimeException("Stub!");
    }

    public void setLinkSpeed(int linkSpeed) {
        throw new RuntimeException("Stub!");
    }

    public void setFrequency(int frequency) {
        throw new RuntimeException("Stub!");
    }

    public void setNetworkId(int id) {
        throw new RuntimeException("Stub!");
    }

    public void setSupplicantState(SupplicantState state) {
        throw new RuntimeException("Stub!");
    }

    public void setInetAddress(InetAddress address) {
        throw new RuntimeException("Stub!");
    }
    public void setScore(int score) {
        throw new RuntimeException("Stub!");
    }
}
