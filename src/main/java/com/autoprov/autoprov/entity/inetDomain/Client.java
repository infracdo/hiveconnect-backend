package com.autoprov.autoprov.entity.inetDomain;

import java.sql.Date;

import org.springframework.stereotype.Indexed;

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
@Entity // labels an entity that can be used for JPA
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "account_no")
    private String accountNumber; // related to subscriber "clients" table

    @Column(name = "name")
    private String clientName;

    @Column(name = "ip_assigned", unique = true)
    private String ipAssigned;

    @Column(name = "onu_serial_number", unique = true)
    private String onuSerialNumber;

    @Column(name = "area_id")
    private Integer site;

    @Column(name = "olt_ip")
    private String oltIp;

    @Column(name = "olt_interface")
    private String oltInterface;

    @Column(name = "modem_mac_address", unique = true)
    private String onuMacAddress;

    @Column(name = "subscription_name", unique = true)
    private String onuDeviceName;

    @Column(name = "package_id")
    private String packageTypeId;

    @Column(name = "backend")
    private String backend;

    @Column(name = "status")
    private String status;

    @Column(name = "ssid_name")
    private String ssidName;

    // public void assignIp(String ip) {
    // this.ip_assigned = ip;
    // }

    // public void setOnuSerialNo(String serialNo) {
    // this.onu_serial_no = serialNo;
    // }

    // @Column(name = "date_assigned")
    // private Date date_assigned;

}
