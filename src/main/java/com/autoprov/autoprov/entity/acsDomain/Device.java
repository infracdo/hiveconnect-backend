package com.autoprov.autoprov.entity.acsDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "device_name")
    private String device_name;

    @Column(name = "mac_address")
    private String mac_address;

    @Column(name = "serial_number")
    private String serial_number;

    @Column(name = "location")
    private String location;

    @Column(name = "parent")
    private String parent;

    @Column(name = "date_created")
    private String date_created;

    @Column(name = "date_modified")
    private String date_modified;

    @Column(name = "date_offline")
    private String date_offline;

    @Column(name = "activated")
    private Boolean activated;

    @Column(name = "status")
    private String status;

    @Column(name = "model")
    private String model;

    @Column(name = "device_type")
    private String device_type;

    public Long getId() {
        return id;
    }

    public String getdevice_name() {
        return device_name;
    }

    public String getmac_address() {
        return mac_address;
    }

    public String getserial_number() {
        return serial_number;
    }

    public String getlocation() {
        return location;
    }

    public String getparent() {
        return parent;
    }

    public String getdate_created() {
        return date_created;
    }

    public String getdate_modified() {
        return date_modified;
    }

    public String getdate_offline() {
        return date_offline;
    }

    public String getstatus() {
        return status;
    }

    public String getmodel() {
        return model;
    }

    public String getdevice_type() {
        return device_type;
    }

    public Boolean getactivated() {
        return activated;
    }

    public void setdevice_name(String device_name) {
        this.device_name = device_name;
    }

    public void setmac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public void setserial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public void setlocation(String location) {
        this.location = location;
    }

    public void setparent(String parent) {
        this.parent = parent;
    }

    public void setdate_created(String date_created) {
        this.date_created = date_created;
    }

    public void setdate_modified(String date_modified) {
        this.date_modified = date_modified;
    }

    public void setdate_offline(String date_offline) {
        this.date_offline = date_offline;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public void setmodel(String model) {
        this.model = model;
    }

    public void setdevice_type(String device_type) {
        this.device_type = device_type;
    }

    public void setactivated(Boolean activated) {
        this.activated = activated;
    }

    public Device() {
    }

    public Device(String device_name, String mac_address, String serial_number, String location, String parent,
            String date_created, String date_modified, String date_offline, String status, String model,
            String device_type) {
        this.device_name = device_name;
        this.mac_address = mac_address;
        this.serial_number = serial_number;
        this.location = location;
        this.parent = parent;
        this.date_created = date_created;
        this.date_modified = date_modified;
        this.date_offline = date_offline;
        this.status = status;
        this.model = model;
        this.device_type = device_type;
    }
}