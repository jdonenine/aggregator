package com.datastax.hackathon.telemetry.aggregator.controllers.v1;

import com.datastax.hackathon.telemetry.aggregator.model.TelemetryImageObservation;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryImageObservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("api/v1/telemetry-image-observations")
public class TelemetryImageObservationController {
  private final TelemetryImageObservationRepository telemetryImageObservationRepository;

  @Autowired
  public TelemetryImageObservationController(TelemetryImageObservationRepository telemetryImageObservationRepository) {
    this.telemetryImageObservationRepository = telemetryImageObservationRepository;
  }

  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public List<TelemetryImageObservation> getAllTelemetryImageObservations() {
    if (telemetryImageObservationRepository == null) {
      log.error("telemetryImageObservationRepository is invalid, requests cannot be processed.");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    try {
      return telemetryImageObservationRepository.findAll();
    } catch (Exception e) {
      log.error("Unable to retrieve TelemetryObservation records.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
