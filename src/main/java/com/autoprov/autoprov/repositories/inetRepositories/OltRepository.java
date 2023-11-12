package com.autoprov.autoprov.repositories.inetRepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.inetDomain.Olt;

public interface OltRepository extends CrudRepository<Olt, Long> {

    @Query(value = "SELECT * from olt where name=?1", nativeQuery = true)
    Optional<Olt> findByOlt_site(String olt_site);

    @Query(value = "SELECT * from olt where ip=?1", nativeQuery = true)
    Optional<Olt> findByOlt_ip(String olt_ip);
}
