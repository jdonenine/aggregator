package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.TelemetryObservation;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TelemetryObservationRepository extends CassandraRepository<TelemetryObservation, TelemetryObservation.TelemetryObservationPrimaryKey> {
  @Query("SELECT * FROM telemetry_observations WHERE job_id = :jobId AND observed_at >= :after")
  List<TelemetryObservation> findAllByJobIdAfter(UUID jobId, Instant after);
}
