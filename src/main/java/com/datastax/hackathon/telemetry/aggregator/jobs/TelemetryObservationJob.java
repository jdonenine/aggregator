package com.datastax.hackathon.telemetry.aggregator.jobs;

import com.datastax.hackathon.telemetry.aggregator.exceptions.ServiceException;
import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import com.datastax.hackathon.telemetry.aggregator.model.TelemetryImagesObserved;
import com.datastax.hackathon.telemetry.aggregator.model.TelemetryObserved;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryImagesObservedRepository;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryObservedRepository;
import com.datastax.hackathon.telemetry.aggregator.services.TelemetryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class TelemetryObservationJob implements Job {
  private static final ChronoUnit BUCKET_SIZE = ChronoUnit.DAYS;

  @Autowired
  private TelemetryService telemetryService;

  @Autowired
  private TelemetryImagesObservedRepository telemetryImagesObservedRepository;

  @Autowired
  private TelemetryObservedRepository telemetryObservedRepository;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (telemetryService == null) {
      throw new JobExecutionException("TelemetryService is not available, job cannot be executed.");
    }

    List<Telemetry> telemetries = null;
    try {
      telemetries = telemetryService.getAllTelemetries();
    } catch (ServiceException e) {
      throw new JobExecutionException("Unable to retrieve Telemetry records.", e);
    }

    List<TelemetryObserved> telemetryObservations = Lists.newArrayList();
    List<TelemetryImagesObserved> telemetryImagesObservations = Lists.newArrayList();

    Map<Instant, List<Telemetry>> telemetriesByCreatedAt = bucketizeTelemetryByCreatedAt(telemetries, BUCKET_SIZE);

    for (Instant bucket : telemetriesByCreatedAt.keySet()) {
      List<Telemetry> telemetriesInBucket = telemetriesByCreatedAt.get(bucket);
      if (telemetriesInBucket == null) {
        telemetriesInBucket = Lists.newArrayList();
      }

      Set<UUID> customerIdsInBucket = Sets.newHashSet();
      Set<String> clusterIdsInBucket = Sets.newHashSet();
      Set<String> imageIdsInBucket = Sets.newHashSet();
      Map<String, Set<UUID>> customerIdsInBucketByImageId = Maps.newHashMap();
      Map<String, Set<String>> clusterIdsInBucketByImageId = Maps.newHashMap();

      for (Telemetry telemetry : telemetriesInBucket) {
        if (telemetry.getImage() != null) {
          imageIdsInBucket.add(telemetry.getImage());
        }

        if (telemetry == null || telemetry.getCustomerId() == null) {
          continue;
        }

        customerIdsInBucket.add(telemetry.getCustomerId());

        if (telemetry.getImage() != null && !telemetry.getImage().isEmpty()) {
          Set<UUID> customerIdsInBucketForImageId = customerIdsInBucketByImageId.getOrDefault(telemetry.getImage(), Sets.newHashSet());
          customerIdsInBucketForImageId.add(telemetry.getCustomerId());
          customerIdsInBucketByImageId.put(telemetry.getImage(), customerIdsInBucketForImageId);
        }

        if (telemetry.getK8sClusterId() != null && !telemetry.getK8sClusterId().isEmpty()) {
          clusterIdsInBucket.add(telemetry.getCustomerId() + "/" + telemetry.getK8sClusterId());

          if (telemetry.getImage() != null && !telemetry.getImage().isEmpty()) {
            Set<String> clusterIdsInBucketForImageId = clusterIdsInBucketByImageId.getOrDefault(telemetry.getImage(), Sets.newHashSet());
            clusterIdsInBucketForImageId.add(telemetry.getCustomerId() + "/" + telemetry.getK8sClusterId());
            clusterIdsInBucketByImageId.put(telemetry.getImage(), clusterIdsInBucketForImageId);
          }
        }
      }

      TelemetryObserved telemetryObserved = new TelemetryObserved();
      TelemetryObserved.TelemetryObservedPrimaryKey telemetryObservedPrimaryKey = new TelemetryObserved.TelemetryObservedPrimaryKey();
      telemetryObservedPrimaryKey.setObservedAt(bucket);
      telemetryObservedPrimaryKey.setJobId(UUID.nameUUIDFromBytes(context.getJobDetail().getKey().getName().getBytes()));
      telemetryObserved.setPrimaryKey(telemetryObservedPrimaryKey);
      telemetryObserved.setNumCustomers(customerIdsInBucket.size());
      telemetryObserved.setNumClusters(clusterIdsInBucket.size());
      telemetryObservations.add(telemetryObserved);

      for (String imageId : imageIdsInBucket) {
        Set<UUID> customerIdsForImageId = customerIdsInBucketByImageId.getOrDefault(imageId, Sets.newHashSet());
        Set<String> clusterIdsForImageId = clusterIdsInBucketByImageId.getOrDefault(imageId, Sets.newHashSet());
        TelemetryImagesObserved telemetryImagesObserved = new TelemetryImagesObserved();
        TelemetryImagesObserved.TelemetryImagesObservedPrimaryKey telemetryImagesObservePrimaryKey = new TelemetryImagesObserved.TelemetryImagesObservedPrimaryKey();
        telemetryImagesObservePrimaryKey.setImageId(UUID.nameUUIDFromBytes(imageId.getBytes()));
        telemetryImagesObservePrimaryKey.setObservedAt(bucket);
        telemetryImagesObserved.setPrimaryKey(telemetryImagesObservePrimaryKey);
        telemetryImagesObserved.setNumClusters(clusterIdsForImageId.size());
        telemetryImagesObserved.setNumCustomers(customerIdsForImageId.size());
        telemetryImagesObserved.setImageName(imageId);
        telemetryImagesObservations.add(telemetryImagesObserved);
      }
    }

    try {
      telemetryObservedRepository.saveAll(telemetryObservations);
    } catch (Exception e) {
      throw new JobExecutionException("Unable to save TelemetryObserved records.", e);
    }

    try {
      telemetryImagesObservedRepository.saveAll(telemetryImagesObservations);
    } catch (Exception e) {
      throw new JobExecutionException("Unable to save TelemetryImagesObserved records.", e);
    }
  }

  private static Map<Instant, List<Telemetry>> bucketizeTelemetryByCreatedAt(List<Telemetry> telemetries, ChronoUnit bucketSize) {
    if (telemetries == null) {
      return null;
    }
    if (bucketSize == null) {
      throw new IllegalArgumentException("bucketSize provided must not be null.");
    }

    Map<Instant, List<Telemetry>> results = Maps.newHashMap();
    telemetries.stream()
        .filter(telemetry -> telemetry != null && telemetry.getCreatedAt() != null)
        .forEach(telemetry -> {
          Instant createdAtBucket = telemetry.getCreatedAt().truncatedTo(ChronoUnit.DAYS);
          List<Telemetry> telemetryList = results.get(createdAtBucket);
          if (telemetryList == null) {
            telemetryList = Lists.newArrayList(telemetry);
          } else {
            telemetryList.add(telemetry);
          }
          results.put(createdAtBucket, telemetryList);
        });

    return results;
  }
}
