package com.autoprov.autoprov.entity.ipamDomain;

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
@Table(name = "cidr_block")
public class CidrBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "cidr_block", unique = true)
    private String cidrBlock;

    @Column(name = "account_no")
    private String accountNumber; // related to subscriber "clients" table

    @Column(name = "network_address", unique = true)
    private String networkAddress;

    @Column(name = "network_type")
    private String type;
    private String site;
    private String size;
    private Integer vlanId;
    private String notes;

}
