# aggregator

Hackathon Telemetry Aggregator Service

## Overview

This service is responsible for periodically calculating aggregation of telemetry data.

It accesses data from `telemetry.telemetry` and bucketizes aggregate data into daily sampeles that can be visualized within Grafana using the Cassandra plugin.

This data is published into two tables.

`telemetry_observations` -- captures aggregate data across all samples.  This table is keyed off `job_id` as a means to meet the requirements of the Cassandra/Grafana plugin.

```cql
CREATE TABLE telemetry.telemetry_observations (
    job_id uuid,
    observed_at timestamp,
    num_clusters int,
    num_customers int,
    PRIMARY KEY (job_id, observed_at)
) WITH CLUSTERING ORDER BY (observed_at ASC)
    AND additional_write_policy = '99PERCENTILE'
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.UnifiedCompactionStrategy'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair = 'BLOCKING'
    AND speculative_retry = '99PERCENTILE';
```

`telemetry_image_observations` -- captures aggregate data for each `imageId` identified within the telemetry samples.

```cql
CREATE TABLE telemetry.telemetry_image_observations (
    image_id uuid,
    observed_at timestamp,
    image_name text,
    num_clusters int,
    num_customers int,
    PRIMARY KEY (image_id, observed_at)
) WITH CLUSTERING ORDER BY (observed_at ASC)
    AND additional_write_policy = '99PERCENTILE'
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.UnifiedCompactionStrategy'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair = 'BLOCKING'
    AND speculative_retry = '99PERCENTILE';
```

These tables will be created on application startup if not present on the target cluster.

## Implementation

The service is implemented as a Spring Boot application.  It uses Spring Data for all interactions with Astra and leverages Quartz to schedule the recurring aggregation job.

The service exposes a series of API endpoints that can be used to interact with the service.  They can be accessed easily from the SwaggerUI application exposed on port 8080.

## Configuration

The following environment variables can be provided to configure the application:

### Required

`ASTRA_DB_CLIENT_ID` -- The client ID for connecting to Astra.

`ASTRA_DB_CLIENT_SECRET` -- The client secret for connecting to Astra.

`ASTRA_DB_BUNDLE` -- The **path** to the secure connect bundle for connecting to Astra.

### Optional

`ASTRA_DB_KEYSPACE` -- The keyspace to access.  **Defaults to `telemetry`.**

`APP_AGGREGATION_JOB_FREQUENCY_M` -- The frequency at which the aggregation job should be executed, provided as an integer in minutes.  **Defaults to `1`.**

## Grafana Integration

It's possible to use the GrafanaCassandraDatasource to visualize the data produced by this application.

Learn how to integrate here: https://github.com/HadesArchitect/GrafanaCassandraDatasource

**Note**: The GrafanaCassandraDatasource does not work with more recent versions of Grafana, use Grafana v7.3.10.

### Dashboard Definition

The following dashboard provides an example of how this data can be visulized.  Because of the limitations of the Cassandra datasource plugin some UUIDs have to be provided and will need to be updated to match the end target system.

```json
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "gnetId": null,
  "graphTooltip": 0,
  "id": 1,
  "links": [],
  "panels": [
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Hackathon - Telemetry",
      "description": "Number of customers over time",
      "fieldConfig": {
        "defaults": {
          "custom": {}
        },
        "overrides": []
      },
      "fill": 1,
      "fillGradient": 0,
      "gridPos": {
        "h": 6,
        "w": 12,
        "x": 0,
        "y": 0
      },
      "hiddenSeries": false,
      "id": 2,
      "legend": {
        "alignAsTable": false,
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "rightSide": false,
        "show": false,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 2,
      "nullPointMode": "null",
      "options": {
        "alertThreshold": true
      },
      "percentage": false,
      "pluginVersion": "7.3.10",
      "pointradius": 2,
      "points": true,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "columnId": "job_id",
          "columnTime": "observed_at",
          "columnValue": "num_customers",
          "keyspace": "telemetry",
          "refId": "A",
          "table": "telemetry_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "79267deb-aeb1-34a4-886f-0a91f593e1f1"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Customers",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Hackathon - Telemetry",
      "description": "Number of customers over time",
      "fieldConfig": {
        "defaults": {
          "custom": {}
        },
        "overrides": []
      },
      "fill": 1,
      "fillGradient": 0,
      "gridPos": {
        "h": 6,
        "w": 12,
        "x": 12,
        "y": 0
      },
      "hiddenSeries": false,
      "id": 3,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": false,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 2,
      "nullPointMode": "null",
      "options": {
        "alertThreshold": true
      },
      "percentage": false,
      "pluginVersion": "7.3.10",
      "pointradius": 2,
      "points": true,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "columnId": "job_id",
          "columnTime": "observed_at",
          "columnValue": "num_clusters",
          "keyspace": "telemetry",
          "refId": "A",
          "table": "telemetry_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "79267deb-aeb1-34a4-886f-0a91f593e1f1"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Clusters",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": true,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Hackathon - Telemetry",
      "decimals": null,
      "fieldConfig": {
        "defaults": {
          "custom": {
            "align": null,
            "filterable": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          }
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "1c439395-a7fb-37f2-b5c3-e72ffecd825b"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/orchestrator"
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "319de2ee-2de1-3733-b425-9af994537806"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/testimage1"
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "935f4d0d-0b4f-3cf1-a5c7-d656fd9f3bf7"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/testimage2"
              }
            ]
          }
        ]
      },
      "fill": 1,
      "fillGradient": 0,
      "gridPos": {
        "h": 9,
        "w": 24,
        "x": 0,
        "y": 6
      },
      "hiddenSeries": false,
      "id": 7,
      "legend": {
        "avg": false,
        "current": false,
        "hideZero": false,
        "max": false,
        "min": false,
        "rightSide": true,
        "show": true,
        "sideWidth": null,
        "total": false,
        "values": false
      },
      "lines": false,
      "linewidth": 1,
      "nullPointMode": "null",
      "options": {
        "alertThreshold": true
      },
      "percentage": false,
      "pluginVersion": "7.3.10",
      "pointradius": 2,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": true,
      "steppedLine": false,
      "targets": [
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_customers",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "A",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "1c439395-a7fb-37f2-b5c3-e72ffecd825b"
        },
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_customers",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "B",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "319de2ee-2de1-3733-b425-9af994537806"
        },
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_customers",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "C",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "935f4d0d-0b4f-3cf1-a5c7-d656fd9f3bf7"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Customers by Image",
      "tooltip": {
        "shared": true,
        "sort": 2,
        "value_type": "individual"
      },
      "transformations": [],
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": true,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Hackathon - Telemetry",
      "decimals": null,
      "fieldConfig": {
        "defaults": {
          "custom": {
            "align": null,
            "filterable": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          }
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "1c439395-a7fb-37f2-b5c3-e72ffecd825b"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/orchestrator"
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "319de2ee-2de1-3733-b425-9af994537806"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/testimage1"
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "935f4d0d-0b4f-3cf1-a5c7-d656fd9f3bf7"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "registry.cloud.datastax.com/cloud-ondemand/testimage2"
              }
            ]
          }
        ]
      },
      "fill": 1,
      "fillGradient": 0,
      "gridPos": {
        "h": 9,
        "w": 24,
        "x": 0,
        "y": 15
      },
      "hiddenSeries": false,
      "id": 6,
      "legend": {
        "avg": false,
        "current": false,
        "hideZero": false,
        "max": false,
        "min": false,
        "rightSide": true,
        "show": true,
        "sideWidth": null,
        "total": false,
        "values": false
      },
      "lines": false,
      "linewidth": 1,
      "nullPointMode": "null as zero",
      "options": {
        "alertThreshold": true
      },
      "percentage": false,
      "pluginVersion": "7.3.10",
      "pointradius": 2,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": true,
      "steppedLine": false,
      "targets": [
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_clusters",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "A",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "1c439395-a7fb-37f2-b5c3-e72ffecd825b"
        },
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_clusters",
          "filtering": "",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "B",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "319de2ee-2de1-3733-b425-9af994537806"
        },
        {
          "columnId": "image_id",
          "columnTime": "observed_at",
          "columnValue": "num_clusters",
          "hide": false,
          "keyspace": "telemetry",
          "rawQuery": false,
          "refId": "C",
          "table": "telemetry_image_observations",
          "target": "select timestamp, value from keyspace.table where id=123e4567;",
          "type": "timeserie",
          "valueId": "935f4d0d-0b4f-3cf1-a5c7-d656fd9f3bf7"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Clusters by Image",
      "tooltip": {
        "shared": false,
        "sort": 2,
        "value_type": "individual"
      },
      "transformations": [],
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    }
  ],
  "refresh": "5m",
  "schemaVersion": 26,
  "style": "dark",
  "tags": [],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-30d",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "",
  "title": "Customer Telemetry",
  "uid": "ItKUjmpnz",
  "version": 5
}

```