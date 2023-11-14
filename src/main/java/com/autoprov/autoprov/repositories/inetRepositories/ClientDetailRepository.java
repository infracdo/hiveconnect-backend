package com.autoprov.autoprov.repositories.inetRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.inetDomain.Client;
import com.autoprov.autoprov.entity.inetDomain.ClientDetail;

import jakarta.transaction.Transactional;

public interface ClientDetailRepository extends CrudRepository<ClientDetail, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE clients SET modem_mac_address = NULL, ip_assigned = NULL, status = \'New\', subscription_name = NULL, WHERE location = \'Hive Test\'", nativeQuery = true)
    void resetHiveDummy();
}
