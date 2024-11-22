package com.autoprov.autoprov.repositories.subscriberRepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.entity.subscriberDomain.models.ERole;
import com.autoprov.autoprov.entity.subscriberDomain.models.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
