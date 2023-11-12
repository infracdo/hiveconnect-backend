package com.autoprov.autoprov.entity.acsDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "devices")
public class Devices {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "public_ip")
    private String publicIp;

    public String getPublicIp() {
        return publicIp;
    }

    @Column(name = "second_wan_mac")
    private String secondWanMac;

    public String getSecondWanMac() {
        return secondWanMac;
    }

}