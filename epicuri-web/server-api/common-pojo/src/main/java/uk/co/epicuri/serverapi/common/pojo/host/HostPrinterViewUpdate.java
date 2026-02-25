package uk.co.epicuri.serverapi.common.pojo.host;

public class HostPrinterViewUpdate {
    private String ipAddress;
    private String macAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
