package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Olt;

public interface OltRepository extends CrudRepository<Olt, Long> {

    Optional<Olt> findByolt_site(String olt_site);
}
