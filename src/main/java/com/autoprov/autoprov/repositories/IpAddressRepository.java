package com.autoprov.autoprov.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.IpAddress;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    @Query(value = "SELECT * from ipaddresses where status = \"Available\"", nativeQuery = true)
    List<IpAddress> findAllAvailableIp();

    @Query(value = "SELECT * from ipaddresses where ip_address LIKE ?1%", nativeQuery = true)
    List<IpAddress> findAllUnderNetworkAddress(String networkAddress);

    @Modifying
    @Query("update Client u set u.account_no = ?1 where u.ip_address = ?2")
    void associateIpAddressToAccountNumber(String accountNo, String ipAddress);

    Optional<IpAddress> findByipAddress(String ipAddress);

    @Query(value = "SELECT * from ipaddresses where status =\"Available\" LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddress();

}
