package com.datastax.hackathon.telemetry.aggregator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SpringDataConfiguration {
  @Value("${datastax.astra.secure-connect-bundle}")
  private File datastaxAstraSecureConnectBundle;

  @Bean
  public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
    return builder -> builder.withCloudSecureConnectBundle(datastaxAstraSecureConnectBundle.toPath());
  }
}
