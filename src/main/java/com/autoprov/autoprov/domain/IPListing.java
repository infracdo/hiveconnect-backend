package com.autoprov.autoprov.domain;

import org.springframework.stereotype.Indexed;

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
public class IPListing {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IPAddress_id_seq")
    private Long id;

    private String networkAddress;
    private String hostAddress;
    private String status;
    private String clientId;

}
