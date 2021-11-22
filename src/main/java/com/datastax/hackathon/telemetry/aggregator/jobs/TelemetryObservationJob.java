package com.datastax.hackathon.telemetry.aggregator.jobs;

import com.datastax.hackathon.telemetry.aggregator.model.Telemetry;
import com.datastax.hackathon.telemetry.aggregator.model.TelemetryImageObservation;
import com.datastax.hackathon.telemetry.aggregator.model.TelemetryObservation;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryImageObservationRepository;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryObservationRepository;
import com.datastax.hackathon.telemetry.aggregator.repositories.TelemetryRepository;
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
  private TelemetryRepository telemetryRepository;

  @Autowired
  private TelemetryImageObservationRepository telemetryImageObservationRepository;

  @Autowired
  private TelemetryObservationRepository telemetryObservationRepository;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Starting job {}.", context.getJobDetail().getKey().getGroup() + "/" + context.getJobDetail().getKey().getName());

    if (telemetryRepository == null) {
      throw new JobExecutionException("TelemetryRepository is not available, job cannot be executed.");
    }
    if (telemetryImageObservationRepository == null) {
      throw new JobExecutionException("TelemetryImageObservationRepository is not available, job cannot be executed.");
    }
    if (telemetryObservationRepository == null) {
      throw new JobExecutionException("TelemetryObservationRepository is not available, job cannot be executed.");
    }

    // Figure out what time we last gathered data -- we'll start from there
    // If we haven't run before, grab it all effectively
    // This is inefficient, but for the hackathon it will have to do
    // It'll be wasteful but safe though as we'll regenerate the same data
    // This should only happen on the first run of the application
    Instant timeLastObserved = context.getPreviousFireTime() != null ? context.getPreviousFireTime().toInstant() : Instant.EPOCH;

    // We'll need data back to the beginning of the bucket in which our new data will start
    // This will recalculate the first bucket on, which is necessary so that we have all of the unique samples in that time
    Instant startingBucket = timeLastObserved.truncatedTo(BUCKET_SIZE);

    // Get all of the telemetry samples going back to the start of the first bucket we'll cover in this run
    List<Telemetry> telemetries = null;
    try {
      telemetries = telemetryRepository.findAllAfter(startingBucket);
    } catch (Exception e) {
      throw new JobExecutionException("Unable to retrieve Telemetry records.", e);
    }

    log.info("Processing {} telemetry records found back to {}.", telemetries != null ? telemetries.size() : 0, startingBucket);
    if (telemetries == null || telemetries.isEmpty()) {
      return;
    }

    // We'll build data samples that aggregate data generally for the full telemetry system
    List<TelemetryObservation> telemetryObservations = Lists.newArrayList();
    // And for each image in use amongst the telemetry users
    List<TelemetryImageObservation> telemetryImagesObservations = Lists.newArrayList();

    // Place the telemetry samples in time buckets
    // Data will be aggregated to these buckets
    Map<Instant, List<Telemetry>> telemetriesByCreatedAt = bucketizeTelemetryByCreatedAt(telemetries, BUCKET_SIZE);

    // Process each bucket
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

      TelemetryObservation telemetryObservation = new TelemetryObservation();
      TelemetryObservation.TelemetryObservationPrimaryKey telemetryObservationPrimaryKey = new TelemetryObservation.TelemetryObservationPrimaryKey();
      telemetryObservationPrimaryKey.setObservedAt(bucket);
      telemetryObservationPrimaryKey.setJobId(UUID.nameUUIDFromBytes(context.getJobDetail().getKey().getName().getBytes()));
      telemetryObservation.setPrimaryKey(telemetryObservationPrimaryKey);
      telemetryObservation.setNumCustomers(customerIdsInBucket.size());
      telemetryObservation.setNumClusters(clusterIdsInBucket.size());
      telemetryObservations.add(telemetryObservation);

      for (String imageId : imageIdsInBucket) {
        Set<UUID> customerIdsForImageId = customerIdsInBucketByImageId.getOrDefault(imageId, Sets.newHashSet());
        Set<String> clusterIdsForImageId = clusterIdsInBucketByImageId.getOrDefault(imageId, Sets.newHashSet());
        TelemetryImageObservation telemetryImageObservation = new TelemetryImageObservation();
        TelemetryImageObservation.TelemetryImageObservationPrimaryKey telemetryImagesObservePrimaryKey = new TelemetryImageObservation.TelemetryImageObservationPrimaryKey();
        telemetryImagesObservePrimaryKey.setImageId(UUID.nameUUIDFromBytes(imageId.getBytes()));
        telemetryImagesObservePrimaryKey.setObservedAt(bucket);
        telemetryImageObservation.setPrimaryKey(telemetryImagesObservePrimaryKey);
        telemetryImageObservation.setNumClusters(clusterIdsForImageId.size());
        telemetryImageObservation.setNumCustomers(customerIdsForImageId.size());
        telemetryImageObservation.setImageName(imageId);
        telemetryImagesObservations.add(telemetryImageObservation);
      }
    }

    try {
      telemetryObservationRepository.saveAll(telemetryObservations);
    } catch (Exception e) {
      throw new JobExecutionException("Unable to save TelemetryObserved records.", e);
    }

    try {
      telemetryImageObservationRepository.saveAll(telemetryImagesObservations);
    } catch (Exception e) {
      throw new JobExecutionException("Unable to save TelemetryImagesObserved records.", e);
    }

    log.info("Completed job {}.", context.getJobDetail().getKey().getGroup() + "/" + context.getJobDetail().getKey().getName());
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
