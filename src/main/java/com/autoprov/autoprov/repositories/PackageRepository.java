package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.PackageType;

public interface PackageRepository extends CrudRepository<PackageType, Long> {

    @Query(value = "SELECT * from packagetypes where package_type_id=?1", nativeQuery = true)
    Optional<Package> findBypackageTypeId(String packageTypeId);
}
