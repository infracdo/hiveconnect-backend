package com.autoprov.autoprov.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Client;

import jakarta.transaction.Transactional;

public interface ClientRepository extends CrudRepository<Client, Long> {

    List<Client> findAll();

    @Modifying
    @Query("update Client u set u.ipAssigned = ?1, u.onuSerialNumber = ?2 where u.id = ?3")
    void updateClientById(String ipAssigned, String onuSerialNumber, Integer id);

    @Modifying
    @Query("update Client u set u.ipAssigned = ?1 where u.onuSerialNumber = ?2")
    void updateClientByOnuSerialNum(String ipAssigned, String onuSerialNumber);

    @Query(value = "SELECT * from clients where onu_serial_number LIKE ?1%", nativeQuery = true)
    Optional<Client> findClientBySerialNumber(String onuSerialNumber);

}
