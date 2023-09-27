package com.autoprov.autoprov.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.IpAddress;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    // @Query("SELECT * from ipaddresses where u.status = Ready to Assign")
    // List<IpAddress> findAllAvailableIp();
}
