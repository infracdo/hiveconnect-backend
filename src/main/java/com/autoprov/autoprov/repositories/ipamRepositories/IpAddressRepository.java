package com.autoprov.autoprov.repositories.ipamRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.ipamDomain.IpAddress;

import jakarta.transaction.Transactional;

public interface IpAddressRepository extends CrudRepository<IpAddress, Long> {

    @Query(value = "SELECT * from ipaddresses where status = \"Available\"", nativeQuery = true)
    List<IpAddress> findAllAvailableIp();

    @Query(value = "SELECT * from ipaddresses where ip_address LIKE ?1%", nativeQuery = true)
    List<IpAddress> findAllUnderCidrBlock(String cidrBlock);

    @Query(value = "SELECT ip_address from ipaddresses where ip_address LIKE ?1% AND notes LIKE '%OLT IP%'", nativeQuery = true)
    String getOltIpOfIpAddress(String cidrBlock);

    @Query(value = "SELECT ip_address from ipaddresses where ip_address LIKE ?1% AND notes LIKE '%Internet Gateway%'", nativeQuery = true)
    String getGatewayOfIpAddress(String cidrBlock);

    @Modifying
    @Transactional
    @Query("update IpAddress u set u.accountNumber = ?1, u.status = \'Assigned\' , u.notes = \'Assigned to client\' where u.ipAddress = ?2")
    void associateIpAddressToAccountNumber(String accountNumber, String ipAddress);

    @Modifying
    @Transactional
    @Query("update IpAddress u set u.accountNumber = ?1, u.status = \'Reserved\' where u.ipAddress = ?2")
    void reserveIpAddressToAccountNumber(String accountNumber, String ipAddress);

    @Modifying
    @Transactional
    @Query(value = "DELETE from ipaddresses where ip_address LIKE ?1%", nativeQuery = true)
    void deleteIpAddressUnderCidrBlock(String cidrBlock);

    @Query(value = "SELECT * from ipaddresses where status =\"Available\" AND ip_address LIKE ?1% LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddressUnderCidrBlock(String cidrBlock);

    // @Query(value = "SELECT * from hive.ipaddresses where status = \"Available\"
    // and ip_address LIKE\n" + //
    // "(\n" + //
    // "\tSELECT CONCAT(truncated_network_address, '%') FROM\n" + //
    // "\t(\n" + //
    // "\tSELECT \n" + //
    // "\t SUBSTRING_INDEX(network_address, '.', 3) AS truncated_network_address\n"
    // + //
    // "\tFROM \n" + //
    // "\t hive.cidr_block \n" + //
    // "\tWHERE \n" + //
    // "\t site = ?1 AND network_type = \"Private\"\n" + //
    // "\t) AS subquery\n" + //
    // ")\n" + //
    // "LIMIT 1;", nativeQuery = true)
    @Query(value = "SELECT * FROM ipaddresses " +
            "WHERE status = 'Available' " +
            "AND ip_address LIKE (" +
            "  SELECT CONCAT(SUBSTRING_INDEX(network_address, '.', 3), '%') AS pattern " +
            "  FROM cidr_block " +
            "  WHERE site = ?1 AND network_type = ?2" +
            "  ORDER BY CAST(ip_address AS SIGNED)" +
            ") " +
            "LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddressUnderSite(String site, String type);

    @Query(value = "SELECT * from ipaddresses where status =\"Available\" AND type = ?1 ip_address LIKE ?2% LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddressUnderCidrBlockAndType(String type, String cidrBlock);

    @Modifying
    @Transactional
    @Query(value = "DELETE from cidr_block where network_address LIKE ?1%", nativeQuery = true)
    void deleteCidrBlock(String cidrBlock);

    Optional<IpAddress> findByipAddress(String ipAddress);

    @Query(value = "SELECT * from ipaddresses where status =\"Available\" LIMIT 1", nativeQuery = true)
    List<IpAddress> getOneAvailableIpAddress();

}
