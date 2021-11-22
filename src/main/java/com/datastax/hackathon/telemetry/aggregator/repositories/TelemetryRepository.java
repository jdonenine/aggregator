package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TelemetryRepository extends CassandraRepository<Telemetry, Telemetry.TelemetryPrimaryKey> {
  @Query(value = "SELECT * FROM telemetry WHERE created_at >= :after ALLOW FILTERING")
  List<Telemetry> findAllAfter(Instant after);
}
