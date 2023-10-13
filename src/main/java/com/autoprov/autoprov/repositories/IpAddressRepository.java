package com.autoprov.autoprov.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.IpAddress;

import jakarta.transaction.Transactional;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    @Query(value = "SELECT * from ipaddresses where status = \"Available\"", nativeQuery = true)
    List<IpAddress> findAllAvailableIp();

    @Query(value = "SELECT * from ipaddresses where ip_address LIKE ?1%", nativeQuery = true)
    List<IpAddress> findAllUnderNetworkAddress(String networkAddress);

    @Query(value = "SELECT ip_address from ipaddresses where ip_address LIKE ?1% AND notes LIKE '%OLT IP%'", nativeQuery = true)
    String getOltIpOfIpAddress(String networkAddress);

    @Query(value = "SELECT ip_address from ipaddresses where ip_address LIKE ?1% AND notes LIKE '%Internet Gateway%'", nativeQuery = true)
    String getGatewayOfIpAddress(String networkAddress);

    @Modifying
    @Transactional
    @Query("update IpAddress u set u.accountNumber = ?1, u.status = \'Assigned\' , u.notes = \'Assigned to client\' where u.ipAddress = ?2")
    void associateIpAddressToAccountNumber(String accountNumber, String ipAddress);

    @Modifying
    @Transactional
    @Query("update IpAddress u set u.accountNumber = ?1, u.status = \'Reserved\' where u.ipAddress = ?2")
    void reserveIpAddressToAccountNumber(String accountNumber, String ipAddress);

    Optional<IpAddress> findByipAddress(String ipAddress);

    @Query(value = "SELECT * from ipaddresses where status =\"Available\" LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddress();

}
