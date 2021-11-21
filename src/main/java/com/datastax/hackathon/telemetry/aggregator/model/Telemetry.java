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
import java.util.UUID;

@Table(value = "telemetry")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Telemetry {
  /*
    CREATE TABLE telemetry.telemetry (
      id uuid PRIMARY KEY,
      created_at timestamp,
      customer_id uuid,
      image text,
      k8s_cluster_id text,
      kind text,
      manifest text,
      org_name text,
      replication_factor text,
      repository text,
      tag text
    )
   */
  @PrimaryKey
  private TelemetryPrimaryKey primaryKey;

  @Column(value = "created_at")
  @CassandraType(type = CassandraType.Name.TIMESTAMP)
  private Instant createdAt;

  @Column(value = "customer_id")
  @CassandraType(type = CassandraType.Name.UUID)
  private UUID customerId;

  @Column(value = "image")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String image;

  @Column(value = "k8s_cluster_id")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String k8sClusterId;

  @Column(value = "kind")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String kind;

  @Column(value = "manifest")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String manifest;

  @Column(value = "org_name")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String orgName;

  @Column(value = "replication_factor")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String replicationFactor;

  @Column(value = "repository")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String repository;

  @Column(value = "tag")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String tag;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @PrimaryKeyClass
  public static class TelemetryPrimaryKey {
    @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID id;
  }
}
