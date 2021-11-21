package com.datastax.hackathon.telemetry.aggregator.services;

import com.datastax.hackathon.telemetry.aggregator.exceptions.ServiceException;
import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryRepository;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelemetryService {
  private static final int PAGE_SIZE = 100;

  private TelemetryRepository telemetryRepository;

  @Autowired
  public TelemetryService(TelemetryRepository telemetryRepository) {
    this.telemetryRepository = telemetryRepository;
  }

  public List<Telemetry> saveTelemetry(List<Telemetry> telemetries) throws ServiceException {
    if (telemetries == null || telemetries.isEmpty()) {
      throw new IllegalArgumentException("List of Customers provided must not be null or empty.");
    }

    try {
      return telemetryRepository.saveAll(telemetries);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public List<Telemetry> getAllTelemetries() throws ServiceException {
    List<Telemetry> allTelemetries = Lists.newArrayList();

    Slice<Telemetry> page = null;
    try {
      page = telemetryRepository.findAll(Pageable.ofSize(PAGE_SIZE));
      allTelemetries.addAll(page.getContent());
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    while (page != null && page.hasNext()) {
      try {
        page = telemetryRepository.findAll(page.nextPageable());
        allTelemetries.addAll(page.getContent());
      } catch (Exception e) {
        throw new ServiceException(e);
      }
    }

    return allTelemetries;
  }
}
