package com.autoprov.autoprov.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.IpAddress;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    @Query(value = "SELECT * from ipaddresses where status = \"Available\"", nativeQuery = true)
    List<IpAddress> findAllAvailableIp();

    @Query(value = "SELECT * from ipaddresses where ip_address LIKE ?1%", nativeQuery = true)
    List<IpAddress> findAllUnderNetworkAddress(String networkAddress);
}
