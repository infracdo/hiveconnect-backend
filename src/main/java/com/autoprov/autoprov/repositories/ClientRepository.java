package com.autoprov.autoprov.repositories;

import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.domain.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {

}
