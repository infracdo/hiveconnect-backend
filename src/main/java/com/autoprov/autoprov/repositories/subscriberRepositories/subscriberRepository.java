package com.autoprov.autoprov.repositories.subscriberRepositories;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;

import jakarta.transaction.Transactional;

@Repository
public interface subscriberRepository extends JpaRepository<subscriberEntity, Long> {

    List<subscriberEntity>findAll();

    Optional<subscriberEntity> findBySubscriberAccountNumber(String subscriberAccountNumber);
    
    Optional<subscriberEntity> findByPackageType(String packageType);
    

    @Modifying
    @Query("update subscriberEntity u set u.ipAssigned = ?1, u.onuSerialNumber = ?2 where u.id = ?3")
    void updateClientById(String ipAssigned, String onuSerialNumber, Integer id);

    @Modifying
    @Query("update subscriberEntity u set u.ipAssigned = ?1 where u.onuSerialNumber = ?2")
    void updateClientByOnuSerialNum(String ipAssigned, String onuSerialNumber);

    @Query(value = "SELECT * from new_subscriber where onu_serial_number LIKE ?1%", nativeQuery = true)
    Optional<subscriberEntity> findClientBySerialNumber(String onuSerialNumber);

    @Query(value = "SELECT * from new_subscriber where subscriber_status = \'NEW\'", nativeQuery = true)
    List<subscriberEntity> getNewClients();

    @Query(value = "SELECT * from new_subscriber where subscriber_status != 'NEW'", nativeQuery = true)
    List<subscriberEntity> getNonNewClients();

    @Query(value = "SELECT * from new_subscriber where bucket_id = 100", nativeQuery = true)
    List<subscriberEntity> getHiveConnectClients();

    @Modifying
    @Transactional
    @Query(value = "UPDATE clients SET modem_mac_address = NULL, ip_assigned = NULL, status = \'New\', subscription_name = NULL, WHERE location = \'Hive Test\'", nativeQuery = true)
    void resetHiveDummy();

}