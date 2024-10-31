package com.autoprov.autoprov.entity.oltDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;



@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "site_olt") //database table name
public class oltEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "olt_id", unique = true, nullable = false)
    private Long newoltId;

    @Column(name = "olt_name", nullable = false)
    private String oltName;

    @Column(name = "olt_ipaddress", nullable = false)
    private String oltIp;

    @Column(name = "olt_network_site")
    private String oltNetworksite;


 // Default constructor
 public oltEntity() {}
 // Getters and Setters
 public Long getNewOltId() {
     return newoltId;
 }
 public void setNewOltId(Long newOltId) {
     this.newoltId = newOltId;
 }
 public String getOltName() {
     return oltName;
 }
 public void setOltName(String oltName) {
     this.oltName = oltName;
 }
 public String getOltIp() {
     return oltIp;
 }
 public void setOltIp(String oltIp) {
     this.oltIp = oltIp;
 }
 public String getOltNetworksite() {
     return oltNetworksite;
 }
 public void setOltNetworksite(String oltNetworksite) {
     this.oltNetworksite = oltNetworksite;
 }
 
}

