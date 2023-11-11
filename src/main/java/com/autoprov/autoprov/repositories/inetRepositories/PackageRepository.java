package com.autoprov.autoprov.repositories.inetRepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.inetDomain.PackageType;

public interface PackageRepository extends CrudRepository<PackageType, Long> {

    @Query(value = "SELECT * from packages where id=?1", nativeQuery = true)
    Optional<PackageType> findBypackageId(String packageId);

    @Query(value = "SELECT * from packages where name=?1", nativeQuery = true)
    Optional<PackageType> findBypackageName(String packageName);

    @Query(value = "SELECT max_speed, cir from packages where package_type_id=?1", nativeQuery = true)
    Optional<PackageType> getSpeedsByPackageId(String packageId);

}
