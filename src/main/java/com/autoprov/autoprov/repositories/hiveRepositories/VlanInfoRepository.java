package com.autoprov.autoprov.repositories.hiveRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.autoprov.autoprov.entity.hiveDomain.VlanInfo;

public interface VlanInfoRepository extends JpaRepository<VlanInfo, Long> {
    boolean existsByAccountNo(String accountNo);
    boolean existsByVlanId(String vlanId);
    boolean existsByLocation(String location);

    @Query(value = "SELECT * from vlan_info", nativeQuery = true)
    List<VlanInfo> findAll();

    @Query(value = "SELECT * from vlan_info where account_no = ?1", nativeQuery = true)
    Optional<VlanInfo> findByAccountNo(String accountNo);

    @Query(value = "SELECT * from vlan_info where vlan_id = ?1", nativeQuery = true)
    Optional<VlanInfo> findByVlanId(String vlanId);

    @Query(value = "SELECT * from vlan_info where location = ?1", nativeQuery = true)
    Optional<VlanInfo> findByLocation(String location);

    @Modifying
    @Query("UPDATE VlanInfo c SET c.vlanId = ?2 WHERE c.accountNo = ?1")
    void updateVlanId(String accountNo, String vlanId);

    @Modifying
    @Query("UPDATE VlanInfo c SET c.location = ?2 WHERE c.accountNo = ?1")
    void updateLocation(String accountNo, String location);

    @Modifying
    @Query("update VlanInfo c SET c.vlanId = ?1, c.location = ?2 where c.accountNo = ?3")
    void updateClientById(String vlanId, String location, String accountNo);
}
