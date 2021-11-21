package com.datastax.hackathon.telemetry.aggregator.exceptions;

public class ServiceException extends Throwable {
  public ServiceException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public ServiceException(Throwable throwable) {
    super(throwable);
  }

  public ServiceException(String message) {
    super(message);
  }
}
