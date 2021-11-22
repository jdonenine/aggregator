package com.datastax.hackathon.telemetry.aggregator.configuration.scheduling;

import com.datastax.hackathon.telemetry.aggregator.configuration.ApplicationConfiguration;
import com.datastax.hackathon.telemetry.aggregator.jobs.TelemetryObservationJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Slf4j
public class AggregationScheduler {
  private final SchedulerFactoryBean schedulerFactoryBean;
  private final ApplicationConfiguration applicationConfiguration;

  @Autowired
  public AggregationScheduler(ApplicationConfiguration applicationConfiguration, SchedulerFactoryBean schedulerFactoryBean) {
    this.applicationConfiguration = applicationConfiguration;
    this.schedulerFactoryBean = schedulerFactoryBean;

    initializeConfiguredSchedules();
  }

  private void initializeConfiguredSchedules() {
    String telemetryJobGroup = "telemetry";

    Integer telemetryObservationJobFrequencyM = applicationConfiguration.getAggregationJobFrequencyM() != null ? applicationConfiguration.getAggregationJobFrequencyM() : 1;
    String telemetryObservationJobId = "telemetry-observation";
    String telemetryObservationJobDescription = "Collect and aggregate telemetry data in time buckets";
    JobDetail customerAggregationJobDetail = JobBuilder.newJob(TelemetryObservationJob.class).withIdentity(telemetryObservationJobId, telemetryJobGroup).withDescription(telemetryObservationJobDescription).build();

    Trigger telemetryObservationJobTrigger = newTrigger().
        withIdentity(telemetryObservationJobId, telemetryJobGroup).
        withSchedule(
            simpleSchedule().
                withIntervalInMinutes(telemetryObservationJobFrequencyM).
                repeatForever()
        ).build();

    try {
      schedulerFactoryBean.getScheduler().scheduleJob(customerAggregationJobDetail, telemetryObservationJobTrigger);
      log.info("Scheduled job '{}/{}'.", telemetryJobGroup, telemetryObservationJobId);
    } catch (SchedulerException e) {
      log.error("Unable to schedule job '{}/{}'.", telemetryJobGroup, telemetryObservationJobId, e);
    }
  }
}
