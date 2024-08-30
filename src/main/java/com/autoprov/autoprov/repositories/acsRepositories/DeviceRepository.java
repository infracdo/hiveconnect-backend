package com.autoprov.autoprov.repositories.acsRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.acsDomain.Device;

import jakarta.transaction.Transactional;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {
    @Query("SELECT d FROM Device d WHERE d.serial_number=?1")
    List<Device> findBySerialNum(String serial_number);

    @Query("SELECT d FROM Device d WHERE d.serial_number=?1 AND d.parent=\'unassigned\'")
    List<Device> findBySerialNumOnRogue(String serial_number);

    @Query("SELECT d FROM Device d WHERE d.parent=?1")
    List<Device> findByGroup(String parent);

    @Query("SELECT d FROM Device d WHERE d.serial_number=?1")
    Device getBySerialNum(String serial_number);

    @Modifying
    @Transactional
    @Query(value = "update device set parent = ?1 where serial_number=?2", nativeQuery = true)
    void updateParentBySerialNumber(String parent, String onuSerialNumber);

    @Modifying
    @Transactional
    @Query("update Device u set u.parent = \'unassigned\' where u.parent LIKE \'Hive Test\'")
    void resetHiveDummy();

}