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

    @Column(name = "account_No")
    private String account_No; // related to subscriber "clients" table

    @Column(name = "ip_assigned")
    private String ip_assigned;

    @Column(name = "onu_serial_no")
    private String onu_serial_no;

    @Column(name = "package_type_id")
    private String package_type_id;

    public void assignIp(String ip) {
        this.ip_assigned = ip;
    }

    public void setOnuSerialNo(String serialNo) {
        this.onu_serial_no = serialNo;
    }

    // @Column(name = "date_assigned")
    // private Date date_assigned;

}
