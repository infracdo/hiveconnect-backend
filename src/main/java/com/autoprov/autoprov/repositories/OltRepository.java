package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Olt;

public interface OltRepository extends CrudRepository<Olt, Long> {

    @Query(value = "SELECT * from olt where olt_site=?1", nativeQuery = true)
    Optional<Olt> findByOlt_site(String olt_site);
}
