package com.autoprov.autoprov.entity.subscriberDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;



@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "new_subscriber") //database table name
public class subscriberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriber_id", unique = true, nullable = false)
    private Long newsubscriberId;

    @Column(name = "account_Number", unique = true, nullable = false)
    @NotBlank(message = "subscriber ACCOUNT NUMBER is empty")
    private String subscriberAccountNumber;

    @Column(name = "subscriber_Name", nullable = false)
    @NotBlank(message = "subscriber NAME is empty")
    private String subscriberName;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "provision_type")
    private String provision;

    @Column(name = "subscriber_status")
    private String subsstatus;



    //-------------temporary
    @Column(name = "ip_assigned", unique = true)
    private String ipAssigned;

    @Column(name = "onu_serial_number")
    private String onuSerialNumber;

    @Column(name = "area_id")
    private String site;

    @Column(name = "modem_mac_address")
    private String onuMacAddress;

    @Column(name = "subscription_name")
    private String onuDeviceName;

    @Column(name = "package_id")
    private String packageTypeId;

    @Column(name = "bucket_id")
    private String bucketId;

    @Column(name = "olt_upstream")
    private String oltReportedUpstream;

    @Column(name = "olt_downstream")
    private String oltReportedDownstream;

    @Column(name = "ssid_name")
    private String ssidName;

    @Column(name = "olt_ip")
    private String oltIp;

    //----------------------

 // Default constructor
 public subscriberEntity() {}
 // Getters and Setters
 public Long getNewSubscriberId() {
     return newsubscriberId;
 }
 public void setNewSubscriberId(Long newSubscriberId) {
     this.newsubscriberId = newSubscriberId;
 }
 public String getSubscriberAccountNumber() {
     return subscriberAccountNumber;
 }
 public void setSubscriberAccountNumber(String subscriberAccountNumber) {
     this.subscriberAccountNumber = subscriberAccountNumber;
 }
 public String getSubscriberName() {
     return subscriberName;
 }
 public void setSubscriberName(String subscriberName) {
     this.subscriberName = subscriberName;
 }
 public String getPackageType() {
     return packageType;
 }
 public void setPackageType(String packageTypeId) {
     this.packageType = packageTypeId;
 }

 public String getProvision() {
     return provision;
 }
 public void setProvision(String provision) {
     this.provision = provision;
 }

 public String getSubsStatus() {
     return subsstatus;
 }
 public void setSubsStatus(String subsStatus) {
     this.subsstatus = subsStatus;
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

public String getSite() {
    return site;
}

public void setSite(String site) {
    this.site = site;
}

public String getOltIp() {
    return oltIp;
}

public void setOltIp(String oltIp) {
    this.oltIp = oltIp;
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


public String getSsidName() {
    return ssidName;
}

public void setSsidName(String ssidName) {
    this.ssidName = ssidName;
}

}
