package com.datastax.hackathon.telemetry.aggregator.configuration.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

@Configuration
@Slf4j
public class MonitoringSchedulerConfiguration {
  private final ApplicationContext applicationContext;
  private final QuartzProperties quartzProperties;

  @Autowired
  public MonitoringSchedulerConfiguration(ApplicationContext applicationContext, QuartzProperties quartzProperties) {
    this.applicationContext = applicationContext;
    this.quartzProperties = quartzProperties;
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean() {
    MonitoringSchedulerJobFactory jobFactory = new MonitoringSchedulerJobFactory();
    jobFactory.setApplicationContext(applicationContext);

    Properties properties = new Properties();
    properties.putAll(quartzProperties.getProperties());

    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    factory.setOverwriteExistingJobs(true);
    factory.setQuartzProperties(properties);
    factory.setJobFactory(jobFactory);
    return factory;
  }
}

