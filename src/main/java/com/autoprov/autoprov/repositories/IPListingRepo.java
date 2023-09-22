package com.autoprov.autoprov.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.autoprov.autoprov.domain.IPListing;

public interface IPListingRepo extends CrudRepository<IPListing, Long> {

}
