package com.datastax.hackathon.telemetry.aggregator.controllers.v1;

import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryRepository;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("api/v1/telemetry")
public class TelemetryController {
  private final TelemetryRepository telemetryRepository;

  @Autowired
  public TelemetryController(TelemetryRepository telemetryRepository) {
    this.telemetryRepository = telemetryRepository;
  }

  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public List<Telemetry> getAllTelemetries() {
    if (telemetryRepository == null) {
      log.error("telemetryRepository is invalid, requests cannot be processed.");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    try {
      return telemetryRepository.findAll();
    } catch (Exception e) {
      log.error("Unable to retrieve Telemetry records.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/rpc/generate-sample-data", produces = APPLICATION_JSON_VALUE)
  public List<Telemetry> createSampleTelemetries() {
    if (telemetryRepository == null) {
      log.error("telemetryRepository is invalid, requests cannot be processed.");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Telemetry telemetry = new Telemetry();
    Telemetry.TelemetryPrimaryKey telemetryPrimaryKey = new Telemetry.TelemetryPrimaryKey();
    telemetryPrimaryKey.setId(UUID.fromString("8feee912-41a4-4a47-8960-5075fdb51e41"));
    telemetry.setPrimaryKey(telemetryPrimaryKey);
    telemetry.setCreatedAt(Instant.parse("2021-11-15T20:52:26.00Z"));
    telemetry.setImage("registry.cloud.datastax.com/cloud-ondemand/orchestrator");
    telemetry.setCustomerId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    telemetry.setK8sClusterId("dmc-dev");
    telemetry.setKind("orchestrator");
    telemetry.setManifest("{\"kind\":\"orchestrator\"}");
    telemetry.setOrgName("hackathon");
    telemetry.setReplicationFactor("something");
    telemetry.setRepository("https://github.com/riptano/cloud-ondemand.git");
    telemetry.setTag("app:orchestrator");

    Telemetry telemetry1 = new Telemetry();
    Telemetry.TelemetryPrimaryKey telemetry1PrimaryKey = new Telemetry.TelemetryPrimaryKey();
    telemetry1PrimaryKey.setId(UUID.randomUUID());
    telemetry1.setPrimaryKey(telemetry1PrimaryKey);
    telemetry1.setCreatedAt(Instant.parse("2021-11-15T20:52:26.00Z"));
    telemetry1.setImage("registry.cloud.datastax.com/cloud-ondemand/testimage1");
    telemetry1.setCustomerId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    telemetry1.setK8sClusterId("dmc-dev");
    telemetry1.setKind("orchestrator");
    telemetry1.setManifest("{\"kind\":\"orchestrator\"}");
    telemetry1.setOrgName("hackathon");
    telemetry1.setReplicationFactor("something");
    telemetry1.setRepository("https://github.com/riptano/cloud-ondemand.git");
    telemetry1.setTag("app:orchestrator");

    Telemetry telemetry2 = new Telemetry();
    Telemetry.TelemetryPrimaryKey telemetry2PrimaryKey = new Telemetry.TelemetryPrimaryKey();
    telemetry2PrimaryKey.setId(UUID.randomUUID());
    telemetry2.setPrimaryKey(telemetry2PrimaryKey);
    telemetry2.setCreatedAt(Instant.parse("2021-11-15T20:52:26.00Z"));
    telemetry2.setImage("registry.cloud.datastax.com/cloud-ondemand/testimage2");
    telemetry2.setCustomerId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    telemetry2.setK8sClusterId("dmc-dev");
    telemetry2.setKind("orchestrator");
    telemetry2.setManifest("{\"kind\":\"orchestrator\"}");
    telemetry2.setOrgName("hackathon");
    telemetry2.setReplicationFactor("something");
    telemetry2.setRepository("https://github.com/riptano/cloud-ondemand.git");
    telemetry2.setTag("app:orchestrator");

    Telemetry telemetry3 = new Telemetry();
    Telemetry.TelemetryPrimaryKey telemetry3PrimaryKey = new Telemetry.TelemetryPrimaryKey();
    telemetry3PrimaryKey.setId(UUID.randomUUID());
    telemetry3.setPrimaryKey(telemetry3PrimaryKey);
    telemetry3.setCreatedAt(Instant.parse("2021-11-16T20:52:26.00Z"));
    telemetry3.setImage("registry.cloud.datastax.com/cloud-ondemand/orchestrator");
    telemetry3.setCustomerId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    telemetry3.setK8sClusterId("dmc-dev");
    telemetry3.setKind("orchestrator");
    telemetry3.setManifest("{\"kind\":\"orchestrator\"}");
    telemetry3.setOrgName("hackathon");
    telemetry3.setReplicationFactor("something");
    telemetry3.setRepository("https://github.com/riptano/cloud-ondemand.git");
    telemetry3.setTag("app:orchestrator");

    Telemetry telemetry4 = new Telemetry();
    Telemetry.TelemetryPrimaryKey telemetry4PrimaryKey = new Telemetry.TelemetryPrimaryKey();
    telemetry4PrimaryKey.setId(UUID.randomUUID());
    telemetry4.setPrimaryKey(telemetry4PrimaryKey);
    telemetry4.setCreatedAt(Instant.parse("2021-11-16T20:52:26.00Z"));
    telemetry4.setImage("registry.cloud.datastax.com/cloud-ondemand/orchestrator");
    telemetry4.setCustomerId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    telemetry4.setK8sClusterId("dmc-prod");
    telemetry4.setKind("orchestrator");
    telemetry4.setManifest("{\"kind\":\"orchestrator\"}");
    telemetry4.setOrgName("hackathon");
    telemetry4.setReplicationFactor("something");
    telemetry4.setRepository("https://github.com/riptano/cloud-ondemand.git");
    telemetry4.setTag("app:orchestrator");

    try {
      return telemetryRepository.saveAll(Lists.newArrayList(telemetry, telemetry1, telemetry2, telemetry3, telemetry4));
    } catch (Exception e) {
      log.error("Unable to save Telemetries.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
