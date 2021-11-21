package com.datastax.hackathon.telemetry.aggregator.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table(value = "telemetry_observed")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryObserved {
  @PrimaryKey
  private TelemetryObservedPrimaryKey primaryKey;

  @Column(value = "num_clusters")
  @CassandraType(type = CassandraType.Name.INT)
  private Integer numClusters;

  @Column(value = "num_customers")
  @CassandraType(type = CassandraType.Name.INT)
  private Integer numCustomers;

  @PrimaryKeyClass
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TelemetryObservedPrimaryKey {
    @PrimaryKeyColumn(name = "job_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID jobId;

    @PrimaryKeyColumn(name = "observed_at", type = PrimaryKeyType.CLUSTERED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Instant observedAt;
  }
}
