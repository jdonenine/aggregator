package com.datastax.hackathon.telemetry.aggregator.services;

import com.datastax.hackathon.telemetry.aggregator.exceptions.ServiceException;
import com.datastax.hackathon.telemetry.aggregator.model.Customer;
import com.datastax.hackathon.telemetry.aggregator.repositories.CustomerRepository;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {
  private static final int PAGE_SIZE = 100;

  private CustomerRepository customerRepository;

  @Autowired
  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public List<Customer> saveCustomers(List<Customer> customers) throws ServiceException {
    if (customers == null || customers.isEmpty()) {
      throw new IllegalArgumentException("List of Customers provided must not be null or empty.");
    }

    try {
      return customerRepository.saveAll(customers);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public List<Customer> getAllCustomers() throws ServiceException {
    List<Customer> allCustomers = Lists.newArrayList();

    Slice<Customer> page = null;
    try {
      page = customerRepository.findAll(Pageable.ofSize(PAGE_SIZE));
      allCustomers.addAll(page.getContent());
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    while (page != null && page.hasNext()) {
      try {
        page = customerRepository.findAll(page.nextPageable());
        allCustomers.addAll(page.getContent());
      } catch (Exception e) {
        throw new ServiceException(e);
      }
    }

    return allCustomers;
  }

  public List<Customer> getAllCustomersCreatedSince(Instant timestamp) throws ServiceException {
    List<Customer> allCustomers = getAllCustomers();
    if (timestamp == null) {
      return allCustomers;
    }

    return allCustomers.stream()
        .filter(customer -> customer != null && customer.getCreatedAt() != null && customer.getCreatedAt().isAfter(timestamp))
        .collect(Collectors.toList());
  }
}
