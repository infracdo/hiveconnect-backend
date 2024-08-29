package com.autoprov.autoprov.repositories.acsRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.acsDomain.Devices;


@Repository
public interface DevicesRepository extends CrudRepository<Devices, Long> {
    @Query(value = "SELECT * FROM devices WHERE serial_num=?1", nativeQuery = true)
    List<Devices> getOnuInfoBySerialNumber(String serial_number);

}