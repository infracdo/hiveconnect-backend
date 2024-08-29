 package com.autoprov.autoprov.services;



 import java.util.Optional;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import com.autoprov.autoprov.entity.subscriberDomain.PackageTypeEntity;
import com.autoprov.autoprov.repositories.subscriberRepositories.PackageRepository;

import jakarta.transaction.Transactional;
 
 @Service
 @Transactional
 public class PackageTypeService {
 
    
    @Autowired
    private PackageRepository packageRepository;

    public PackageTypeEntity savePackage(PackageTypeEntity packageTypeEntity) throws Exception {
        Optional<PackageTypeEntity> existingPackage = packageRepository.findByPackageType
        (packageTypeEntity.getPackageType());
        if (existingPackage.isPresent()) {
            throw new Exception("packages already exists.");
        }
        return packageRepository.save(packageTypeEntity);
    }
 }