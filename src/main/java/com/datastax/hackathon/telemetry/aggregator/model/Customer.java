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

@Table(value = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
  /*
    CREATE TABLE telemetry.customers (
      id uuid PRIMARY KEY,
      created_at timestamp,
      email text,
      org_name text,
      unique_token text
    )
   */
  @PrimaryKey
  private CustomerPrimaryKey primaryKey;

  @Column(value = "created_at")
  @CassandraType(type = CassandraType.Name.TIMESTAMP)
  private Instant createdAt;

  @Column(value = "email")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String email;

  @Column(value = "org_name")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String orgName;

  @Column(value = "unique_token")
  @CassandraType(type = CassandraType.Name.TEXT)
  private String uniqueToken;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @PrimaryKeyClass
  public static class CustomerPrimaryKey {
    @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID id;
  }
}
