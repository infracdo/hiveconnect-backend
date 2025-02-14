package com.autoprov.autoprov.entity.ipamDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cidr_block")
public class CidrBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cidr_id", unique = true,nullable = false)
    private Long id;

    @Column(name = "cidr_block", unique = true,nullable = false)
    private String cidrBlock;

    @Column(name = "default_gateway", unique = true,nullable = false)
    private String defaultGateway;

    @Column(name = "broadcast_address", unique = true,nullable = false)
    private String broadcastAddress;

    @Column(name = "network_address", unique = true,nullable = false)
    private String networkAddress;

    @Column(name = "network_name")
    private String networkName;

    @Column(name = "network_type")
    private String networkType;

    @Column(name = "vlan_id")
    private String vlanId;

    @Column(name = "site_location")
    private String location;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }
}
