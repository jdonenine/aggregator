package com.datastax.hackathon.telemetry.aggregator.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("application")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationConfiguration {
  private Integer aggregationJobFrequencyM;
}
