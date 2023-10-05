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
    @Query("update Client u set u.ip_assigned = ?1, u.onu_serial_no = ?2 where u.id = ?3")
    void updateClientById(String ip_assigned, String onu_serial_no, Integer id);

    @Query(value = "SELECT * from clients where onu_serial_no LIKE ?1%", nativeQuery = true)
    Optional<Client> findClientBySerialNumber(String serial_number);

}
