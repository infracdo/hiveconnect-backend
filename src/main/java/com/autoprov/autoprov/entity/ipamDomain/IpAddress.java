package com.autoprov.autoprov.entity.ipamDomain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
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
@Table(name = "ipaddresses")
public class IpAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "ip_address", unique = true)
    private String ipAddress;

    private String status;

    @Column(name = "account_no")
    private String accountNumber; // related to subscriber "clients" table

    private String type;
    private Integer vlanId;
    private Boolean assignable;
    private String notes;

}
