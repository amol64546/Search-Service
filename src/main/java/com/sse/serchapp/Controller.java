package com.sse.serchapp;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller {

  @Autowired
  EmployeeRepo employeeRepository;

  @GetMapping("/create-random-employees")
  public String createRandomEmployees() {
    int numberOfEmployees = 10; // You can change this number to whatever you like
    createRandomEmployees(numberOfEmployees);
    return numberOfEmployees + " random employees have been created!";
  }

  @GetMapping("/all")
  public List<Employee> all() {

    return employeeRepository.findAll();
  }

  public void createRandomEmployees(int numberOfEmployees) {
    Random random = new Random();

    for (int i = 0; i < numberOfEmployees; i++) {
      Employee employee = new Employee();

      // Generate random data
      employee.setId((long) (random.nextInt(1000) + 1)); // Generate random ID between 1 and 1000
      employee.setName("Employee_" + random.nextInt(100)); // Generate random name
      employee.setEmail("employee" + random.nextInt(1000) + "@example.com"); // Generate random email
      employee.setMobile("987" + (random.nextInt(1000000000) + 100000000)); // Generate random mobile number
      employee.setRole(random.nextBoolean() ? "manager" : "lead"); // Generate random role
      employee.setStatus(random.nextBoolean() ? "Active" : "Inactive"); // Random status
      employee.setAddress("Address_" + random.nextInt(100)); // Generate random address

      // Save the employee to the database
      employeeRepository.save(employee);
    }
  }


  @GetMapping
  public List<Employee> getEmployees(@RequestParam MultiValueMap<String, String> params) {
    Specification<Employee> spec = parseSearchParams(params);
    return employeeRepository.findAll(spec);
  }


  public <T> Specification<T> parseSearchParams(MultiValueMap<String, String> params) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      for (Map.Entry<String, List<String>> param : params.entrySet()) {
        String key = param.getKey();
        List<String> values = param.getValue();
        if (key.equals("page") || key.equals("size") || key.equals("sort")) {
          continue; // skip pagination/sorting params
        }
        try {
          Path<?> path = root.get(key);
          if (values.get(0).startsWith("in:")) {
            String[] valuesArray = values.get(0).substring(3).split(",");
            List<String> trimmedValues = Arrays.stream(valuesArray)
                .map(String::trim)
                .collect(Collectors.toList());
            predicates.add(path.in(trimmedValues));
            continue;
          }
          for (String value : values) {
            if (value.startsWith("eq:")) {
              predicates.add(criteriaBuilder.equal(path, value.substring(3)));
            } else if (value.startsWith("like:")) {
              predicates.add(
                  criteriaBuilder.like(path.as(String.class), "%" + value.substring(5) + "%"));
            } else if (value.startsWith("gt:")) {
              predicates.add(
                  criteriaBuilder.greaterThan(path.as(String.class), value.substring(3)));
            } else if (value.startsWith("gte:")) {
              predicates.add(
                  criteriaBuilder.greaterThanOrEqualTo(path.as(String.class), value.substring(4)));
            } else if (value.startsWith("lt:")) {
              predicates.add(criteriaBuilder.lessThan(path.as(String.class), value.substring(3)));
            } else if (value.startsWith("lte:")) {
              predicates.add(
                  criteriaBuilder.lessThanOrEqualTo(path.as(String.class), value.substring(4)));
            }
          }
        } catch (Exception e) {
          log.error("Error creating predicate for {}:{}", key, values, e);
        }
      }
      return predicates.isEmpty()
          ? criteriaBuilder.conjunction()
          : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

}
