package com.autoprov.autoprov.repositories.hiveRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.hiveDomain.HiveClient;

import jakarta.transaction.Transactional;

public interface HiveClientRepository extends CrudRepository<HiveClient, Long> {

    List<HiveClient> findAll();

    Optional<HiveClient> findByAccountNumber(String accountNumber);

    @Modifying
    @Query("update HiveClient u set u.ipAssigned = ?1, u.onuSerialNumber = ?2 where u.id = ?3")
    void updateClientById(String ipAssigned, String onuSerialNumber, Integer id);

    @Modifying
    @Query("update HiveClient u set u.ipAssigned = ?1 where u.onuSerialNumber = ?2")
    void updateClientByOnuSerialNum(String ipAssigned, String onuSerialNumber);

    @Query(value = "SELECT * from clients where onu_serial_number LIKE ?1%", nativeQuery = true)
    Optional<HiveClient> findClientBySerialNumber(String onuSerialNumber);

    @Query(value = "SELECT * from clients where status = \'New\'", nativeQuery = true)
    List<HiveClient> getNewClients();

    @Query(value = "SELECT * from clients where backend = \'HiveConnect\' or backend = \'Hive Connect\' ", nativeQuery = true)
    List<HiveClient> getHiveConnectClients();

    @Modifying
    @Transactional
    @Query(value = "UPDATE clients SET modem_mac_address = NULL, ip_assigned = NULL, status = \'New\', subscription_name = NULL, backend = NULL, olt_interface = NULL, olt_ip = NULL, onu_serial_number = NULL, ssid_name = NULL, ssid_pw = NULL WHERE location LIKE \'Hive Test\'", nativeQuery = true)
    void resetHiveDummy();

}
