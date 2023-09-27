package com.autoprov.autoprov.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.IpAddress;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    @Query(value = "SELECT * from IpAddresses where status = \"Ready to Assign\"", nativeQuery = true)
    List<IpAddress> findAllAvailableIp();
}
