package com.autoprov.autoprov.repositories.hiveRepositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.hiveDomain.HiveClient;

import jakarta.transaction.Transactional;

public interface HiveClientRepository extends CrudRepository<HiveClient, Long> {
    @Query(value = "SELECT * from hive_clients", nativeQuery = true)
    List<HiveClient> findAll();

    @Query(value = "SELECT * from hive_clients where status = \'ACTIVE'", nativeQuery = true)
    List<HiveClient> findAllActive();

    @Query(value = "SELECT * from hive_clients where status = \'ONHOLD'", nativeQuery = true)
    List<HiveClient> findAllOnhold();

    @Query(value = "SELECT * from hive_clients where status IN ('ACTIVE', 'ONHOLD')", nativeQuery = true)
    List<HiveClient> findActiveHold();
    
    @Query(value = "SELECT * from hive_clients where status LIKE '%\\_PENDING_MIGRATION'", nativeQuery = true)
    List<HiveClient> findMigrating();

   Optional<HiveClient> findBySubscriberAccountNumber(String subscriberAccountNumber);

    //HiveClient findByAccountNumber(String accountNumber);

    @Modifying
    @Query("update HiveClient u set u.ipAssigned = ?1, u.onuSerialNumber = ?2 where u.id = ?3")
    void updateClientById(String ipAssigned, String onuSerialNumber, Integer id);

    @Modifying
    @Query("update HiveClient u set u.ipAssigned = ?1 where u.onuSerialNumber = ?2")
    void updateClientByOnuSerialNum(String ipAssigned, String onuSerialNumber);

    @Query(value = "SELECT * from hive_clients where account_no = ?1 AND status = ?2", nativeQuery = true)
    Optional<HiveClient> findClientByAccountNoStatus(String account_no, String status);

    @Query(value = "SELECT * from hive_clients where onu_serial_number LIKE ?1%", nativeQuery = true)
    Optional<HiveClient> findClientBySerialNumber(String onuSerialNumber);

    @Query(value = "SELECT * from hive_clients where onu_serial_number LIKE ?1%", nativeQuery = true)
    Optional<HiveClient> findClientByStatus(String onuSerialNumber);

    @Query(value = "SELECT COUNT(*) from hive_clients where status LIKE ?1", nativeQuery = true)
    Long countClientsByStatus(String status);

    // @Query(value = "SELECT * from hive_clients where status = \'NEW\'", nativeQuery = true)
    // List<HiveClient> getNewClients();

    @Query(value = "SELECT * from hive_clients where backend = \'HiveConnect\' or backend = \'Hive Connect\' ", nativeQuery = true)
    List<HiveClient> getHiveConnectClients();

    @Modifying
    @Transactional
    @Query(value = "UPDATE hive_clients SET modem_mac_address = NULL, ip_assigned = NULL, status = \'New\', subscription_name = NULL, backend = NULL, olt_interface = NULL, olt_ip = NULL, onu_serial_number = NULL, ssid_name = NULL, ssid_pw = NULL WHERE location LIKE \'Hive Test\'", nativeQuery = true)
    void resetHiveDummy();

    @Modifying
    @Query("UPDATE HiveClient c SET c.status = ?2 WHERE c.subscriberAccountNumber = ?1")
    void updateClientStatus(String accountNumber, String status);


    @Query("SELECT new map(h.subscriberAccountNumber as subscriberAccountNumber, h.clientName as clientName, h.site as site, h.provision as provision, h.status as status) " +
           "FROM HiveClient h WHERE h.status = 'Active' OR h.status = 'Activated'")
    List<Map<String, Object>> findClientsByStatus();
}