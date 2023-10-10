package com.autoprov.autoprov.domain;

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
@Table(name = "networkaddresses")
public class NetworkAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "network_address", unique = true)
    private String networkAddress;

    private String account_No; // related to subscriber "clients" table

    @Column(name = "network_type")
    private String type;
    private String site;
    private Integer vlanId;
    private String notes;

}
