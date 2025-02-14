package com.autoprov.autoprov.dto;

public class CidrBlockDTO {
    private String cidrBlock;
    private String defaultGateway;
    private String broadcastAddress;
    private String networkAddress;
    private String networkName;
    private String networkType;
    private String vlanId;
    private String location;

    // Getters and setters
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getVlanId() {
        return vlanId;
    }
    public void setVlanId(String vlanId) {
        this.vlanId = vlanId;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    
}
