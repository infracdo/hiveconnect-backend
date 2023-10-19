package com.autoprov.autoprov.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.autoprov.autoprov.domain.CidrBlock;

public interface CidrBlockRepository extends CrudRepository<CidrBlock, Long> {

    Optional<CidrBlock> findByCidrBlock(String networkAddress);

}
