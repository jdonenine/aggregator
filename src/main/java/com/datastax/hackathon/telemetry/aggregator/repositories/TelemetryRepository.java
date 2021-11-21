package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryRepository extends CassandraRepository<Telemetry, Telemetry.TelemetryPrimaryKey> {
}
