package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Olt;
import java.util.List;

public interface OltRepository extends CrudRepository<Olt, Long> {

    Optional<Olt> findByOlt_site(String olt_site);
}
