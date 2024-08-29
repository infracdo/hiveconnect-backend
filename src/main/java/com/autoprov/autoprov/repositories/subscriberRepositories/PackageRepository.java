 package com.autoprov.autoprov.repositories.subscriberRepositories;

 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

 import com.autoprov.autoprov.entity.subscriberDomain.PackageTypeEntity;

@Repository
public interface PackageRepository extends JpaRepository<PackageTypeEntity, Long> {
    
    
    List<PackageTypeEntity>findAll();
     Optional<PackageTypeEntity> findByPackageType(String packageType);

    @Query(value = "SELECT * from new_packages where package_type=?1", nativeQuery = true)
    Optional<PackageTypeEntity> findBypackageId(String packageType);

    @Query(value = "SELECT * from new_packages where package_type=?1", nativeQuery = true)
    Optional<PackageTypeEntity> findBypackageName(String packageType);

    @Query(value = "SELECT max_speed, cir from new_packages where package_type=?1", nativeQuery = true)
    Optional<PackageTypeEntity> getSpeedsByPackageId(String packageType);

}