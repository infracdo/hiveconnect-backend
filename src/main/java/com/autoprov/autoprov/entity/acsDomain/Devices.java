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

    @Column(name = "second_wan_mac")
    private String secondWanMac;

    // Default constructor
    public Devices() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getSecondWanMac() {
        return secondWanMac;
    }

    public void setSecondWanMac(String secondWanMac) {
        this.secondWanMac = secondWanMac;
    }

    @Override
    public String toString() {
        return "Devices{" +
                "id=" + id +
                ", publicIp='" + publicIp + '\'' +
                ", secondWanMac='" + secondWanMac + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Devices devices = (Devices) o;

        if (!id.equals(devices.id)) return false;
        if (!publicIp.equals(devices.publicIp)) return false;
        return secondWanMac.equals(devices.secondWanMac);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + publicIp.hashCode();
        result = 31 * result + secondWanMac.hashCode();
        return result;
    }
}