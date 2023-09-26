package com.autoprov.autoprov.repositories;

import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.NetworkAddress;

public interface NetworkAddressRepository extends CrudRepository<NetworkAddress, Long> {

}
