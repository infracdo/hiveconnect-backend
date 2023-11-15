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
@Table(name = "client_details")
public class ClientDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "client_id")
    private String clientId; // related to subscriber "clients" table

    @Column(name = "status")
    private String status;

    @Column(name = "inst_remarks")
    private String remarks;

    @Column(name = "otc")
    private String otc;

    @Column(name = "date_activated")
    private String date_activated;

    // public void assignIp(String ip) {
    // this.ip_assigned = ip;
    // }

    // public void setOnuSerialNo(String serialNo) {
    // this.onu_serial_no = serialNo;
    // }

    // @Column(name = "date_assigned")
    // private Date date_assigned;

}
