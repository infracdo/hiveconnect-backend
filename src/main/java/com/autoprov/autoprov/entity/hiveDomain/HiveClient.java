package com.autoprov.autoprov.entity.hiveDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;



@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hive_clients")
public class HiveClient {
   
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "account_no")
    private String subscriberAccountNumber;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "ip_assigned", unique = true)
    private String ipAssigned;

    @Column(name = "onu_serial_number")
    private String onuSerialNumber;

    @Column(name = "area_id_site")
    private Integer site;

    @Column(name = "olt_ip")
    private String oltIp;

    @Column(name = "olt_interface")
    private String oltInterface;

    @Column(name = "modem_mac_address")
    private String onuMacAddress;

    @Column(name = "subscription_name")
    private String onuDeviceName;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "olt_upstream")
    private String oltReportedUpstream;

    @Column(name = "olt_downstream")
    private String oltReportedDownstream;

    @Column(name = "backend")
    private String provision;

    @Column(name = "status")
    private String status;

    @Column(name = "ssid_name")
    private String ssidName;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriberAccountNumber() {
        return subscriberAccountNumber;
    }

    public void setSubscriberAccountNumber(String subscriberAccountNumber) {
        this.subscriberAccountNumber = subscriberAccountNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getIpAssigned() {
        return ipAssigned;
    }

    public void setIpAssigned(String ipAssigned) {
        this.ipAssigned = ipAssigned;
    }

    public String getOnuSerialNumber() {
        return onuSerialNumber;
    }

    public void setOnuSerialNumber(String onuSerialNumber) {
        this.onuSerialNumber = onuSerialNumber;
    }

    public Integer getSite() {
        return site;
    }

    public void setSite(Integer site) {
        this.site = site;
    }

    public String getOltIp() {
        return oltIp;
    }

    public void setOltIp(String oltIp) {
        this.oltIp = oltIp;
    }

    public String getOltInterface() {
        return oltInterface;
    }

    public void setOltInterface(String oltInterface) {
        this.oltInterface = oltInterface;
    }

    public String getOnuMacAddress() {
        return onuMacAddress;
    }

    public void setOnuMacAddress(String onuMacAddress) {
        this.onuMacAddress = onuMacAddress;
    }

    public String getOnuDeviceName() {
        return onuDeviceName;
    }

    public void setOnuDeviceName(String onuDeviceName) {
        this.onuDeviceName = onuDeviceName;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getOltReportedUpstream() {
        return oltReportedUpstream;
    }

    public void setOltReportedUpstream(String oltReportedUpstream) {
        this.oltReportedUpstream = oltReportedUpstream;
    }

    public String getOltReportedDownstream() {
        return oltReportedDownstream;
    }

    public void setOltReportedDownstream(String oltReportedDownstream) {
        this.oltReportedDownstream = oltReportedDownstream;
    }

    public String getProvision() {
        return provision;
    }

    public void setProvision(String provision) {
        this.provision = provision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSsidName() {
        return ssidName;
    }

    public void setSsidName(String ssidName) {
        this.ssidName = ssidName;
    }

    public HiveClient() {
        //TODO Auto-generated constructor stub
    }
}