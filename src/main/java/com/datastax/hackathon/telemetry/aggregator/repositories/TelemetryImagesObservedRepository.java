package com.datastax.hackathon.telemetry.aggregator.repositories;

import com.datastax.hackathon.telemetry.aggregator.model.TelemetryImagesObserved;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryImagesObservedRepository extends CassandraRepository<TelemetryImagesObserved, TelemetryImagesObserved.TelemetryImagesObservedPrimaryKey> {
}
