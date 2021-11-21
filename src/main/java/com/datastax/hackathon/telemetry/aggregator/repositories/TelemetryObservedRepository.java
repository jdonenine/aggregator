package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.TelemetryObserved;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryObservedRepository extends CassandraRepository<TelemetryObserved, TelemetryObserved.TelemetryObservedPrimaryKey> {
}
