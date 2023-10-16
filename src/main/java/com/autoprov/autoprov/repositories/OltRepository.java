package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Olt;
import com.autoprov.autoprov.domain.PackageType;
import java.util.List;

public interface OltRepository extends CrudRepository<Olt, Long> {

    Optional<Olt> findByOlt_site(String olt_site);
}
