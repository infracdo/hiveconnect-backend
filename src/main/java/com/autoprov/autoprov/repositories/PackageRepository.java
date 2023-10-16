package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.PackageType;

public interface PackageRepository extends CrudRepository<PackageType, Long> {

    Optional<PackageType> findBypackageTypeId(String packageTypeId);
}
