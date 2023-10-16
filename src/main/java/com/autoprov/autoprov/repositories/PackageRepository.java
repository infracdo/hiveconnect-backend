package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.PackageType;

public interface PackageRepository extends CrudRepository<PackageType, Long> {

    Optional<Package> findByPackageTypeId(String packageTypeId);
}
