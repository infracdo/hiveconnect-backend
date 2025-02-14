package com.autoprov.autoprov.repositories.oltRepositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.oltDomain.oltEntity;

@Repository
public interface oltRepository extends JpaRepository<oltEntity, Long> {
    Optional<oltEntity> findByOltName(String oltName);
    Optional<oltEntity> findByOltIp(String oltIp);
    //List<oltEntity> findByOltNetworksite(String networkName);


    @Query(value = "SELECT * from site_olt where olt_name=?1", nativeQuery = true)
    Optional<oltEntity> findByOlt_Name(String olt_Name);

    //@Query(value = "SELECT * from site_olt where olt_ipaddress=?1", nativeQuery = true)
    @Query(value = "SELECT * FROM site_olt WHERE olt_id = ?1", nativeQuery = true)
    Optional<oltEntity> findByOlt_ip(Long olt_id);

    @Query(value = "SELECT * from site_olt", nativeQuery = true)
    List<oltEntity> getAllOlts();

}
