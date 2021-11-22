package com.datastax.hackathon.telemetry.aggregator.controllers.v1;

import com.datastax.hackathon.telemetry.aggregator.model.Customer;
import com.datastax.hackathon.telemetry.aggregator.repositories.CustomerRepository;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {
  private final CustomerRepository customerRepository;

  @Autowired
  public CustomerController(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public List<Customer> getAllCustomers() {
    if (customerRepository == null) {
      log.error("CustomerService is invalid, requests cannot be processed.");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    try {
      return customerRepository.findAll();
    } catch (Exception e) {
      log.error("Unable to retrieve Customer records.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/rpc/generate-sample-data", produces = APPLICATION_JSON_VALUE)
  public List<Customer> createSampleCustomers() {
    if (customerRepository == null) {
      log.error("CustomerService is invalid, requests cannot be processed.");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Customer customer = new Customer();
    Customer.CustomerPrimaryKey customerPrimaryKey = new Customer.CustomerPrimaryKey();
    customerPrimaryKey.setId(UUID.fromString("8c2c24eb-0743-4bbc-b67f-960e8565deea"));
    customer.setPrimaryKey(customerPrimaryKey);
    customer.setCreatedAt(Instant.parse("2021-11-15T20:43:10.00Z"));
    customer.setEmail("pjajoo@example.com");
    customer.setOrgName("hackathon");
    customer.setUniqueToken("96488808-b31b-4f8c-a199-d4195b6b0483");

    Customer customer1 = new Customer();
    Customer.CustomerPrimaryKey customer1PrimaryKey = new Customer.CustomerPrimaryKey();
    customer1PrimaryKey.setId(UUID.randomUUID());
    customer1.setPrimaryKey(customer1PrimaryKey);
    customer1.setCreatedAt(Instant.parse("2021-11-15T22:43:10.00Z"));
    customer1.setEmail("jeff.dinoto@example.com");
    customer1.setOrgName("test1");
    customer1.setUniqueToken("96438808-b31b-4f8c-a199-d419536b0483");

    Customer customer2 = new Customer();
    Customer.CustomerPrimaryKey customer2PrimaryKey = new Customer.CustomerPrimaryKey();
    customer2PrimaryKey.setId(UUID.randomUUID());
    customer2.setPrimaryKey(customer2PrimaryKey);
    customer2.setCreatedAt(Instant.parse("2021-11-16T15:12:10.00Z"));
    customer2.setEmail("test2@example.com");
    customer2.setOrgName("test2");
    customer2.setUniqueToken("96438808-b31b-4f8c-a199-d419536b0483");

    try {
      return customerRepository.saveAll(Lists.newArrayList(customer, customer1, customer2));
    } catch (Exception e) {
      log.error("Unable to save customers.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
