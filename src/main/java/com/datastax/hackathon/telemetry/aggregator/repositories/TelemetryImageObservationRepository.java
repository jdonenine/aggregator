package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.TelemetryImageObservation;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryImageObservationRepository extends CassandraRepository<TelemetryImageObservation, TelemetryImageObservation.TelemetryImageObservationPrimaryKey> {
}
