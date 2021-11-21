package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.Customer;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CassandraRepository<Customer, Customer.CustomerPrimaryKey> {
}
