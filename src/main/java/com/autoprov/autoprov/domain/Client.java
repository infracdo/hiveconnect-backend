package com.autoprov.autoprov.domain;

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

    @Column(name = "accountNumber")
    private String accountNumber; // related to subscriber "clients" table

    @Column(name = "clientName")
    private String clientName;

    @Column(name = "ipAssigned", unique = true)
    private String ipAssigned;

    @Column(name = "onuSerialNumber", unique = true)
    private String onuSerialNumber;

    @Column(name = "site")
    private String site;

    @Column(name = "oltIp")
    private String oltIp;

    @Column(name = "onuMacAddress", unique = true)
    private String onuMacAddress;

    @Column(name = "deviceName", unique = true)
    private String onuDeviceName;

    @Column(name = "packageTypeId")
    private String packageTypeId;

    // public void assignIp(String ip) {
    // this.ip_assigned = ip;
    // }

    // public void setOnuSerialNo(String serialNo) {
    // this.onu_serial_no = serialNo;
    // }

    // @Column(name = "date_assigned")
    // private Date date_assigned;

}
