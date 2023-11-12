package com.autoprov.autoprov.repositories.acsRepositories;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.acsDomain.Device;

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

    @Query("update Device u set u.parent = ?1 where u.serial_number=?1")
    void updateParentBySerialNumber(String parent, String onuSerialNumber);

    @Modifying
    @Query("update Device u set u.parent = \'unassigned\' where u.parent LIKE 'Hive%'")
    void resetHiveDummy();

}
