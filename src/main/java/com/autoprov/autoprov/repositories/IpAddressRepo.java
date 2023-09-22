package com.autoprov.autoprov.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.domain.IpAddress;

public interface IpAddressRepo extends CrudRepository<IpAddress, Long> {

}
